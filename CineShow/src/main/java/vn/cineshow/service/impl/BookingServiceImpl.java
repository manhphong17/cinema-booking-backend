package vn.cineshow.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import vn.cineshow.dto.redis.OrderSessionRequest;
import vn.cineshow.dto.request.booking.SeatSelectRequest;
import vn.cineshow.dto.response.booking.BookingSeatsResponse;
import vn.cineshow.dto.response.booking.SeatHold;
import vn.cineshow.dto.response.booking.SeatTicketDTO;
import vn.cineshow.dto.response.booking.ShowTimeResponse;
import vn.cineshow.dto.response.booking.TicketDetailResponse;
import vn.cineshow.dto.response.booking.TicketResponse;
import vn.cineshow.dto.response.payment.PaymentMethodDTO;
import vn.cineshow.enums.TicketStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.PaymentMethod;
import vn.cineshow.model.Room;
import vn.cineshow.model.Seat;
import vn.cineshow.model.ShowTime;
import vn.cineshow.model.Ticket;
import vn.cineshow.repository.MovieRepository;
import vn.cineshow.repository.PaymentMethodRepository;
import vn.cineshow.repository.ShowTimeRepository;
import vn.cineshow.repository.TicketRepository;
import vn.cineshow.service.BookingService;
import vn.cineshow.service.OrderSessionService;
import vn.cineshow.service.RedisService;
import vn.cineshow.service.SeatHoldService;

@Service
@Slf4j(topic = "BOOKING-SERVICE")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl implements BookingService {
    MovieRepository movieRepository;
    ShowTimeRepository showTimeRepository;
    RedisService redisService;
    SeatHoldService seatHoldService;
    SimpMessagingTemplate messagingTemplate;
    TicketRepository ticketRepository;
    OrderSessionService orderSessionService;
    PaymentMethodRepository paymentMethodRepository;

    @Override
    public List<ShowTimeResponse> getShowTimesByMovieAndDay(Long movieId, LocalDate date) {

        movieRepository.findById(movieId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        return showTimeRepository.findUpcomingShowTimes(
                date,
                LocalDateTime.now(),
                movieId
        );
    }

    @Override
    public List<ShowTimeResponse> getShowTimesByMovieAndStartTime(Long movieId, LocalDateTime startTime) {
        // Return all showtimes, even if sold out, so frontend can display "Hết vé" message
        return showTimeRepository.findByMovie_IdAndStartTime(movieId, startTime).stream()
                .map(s -> ShowTimeResponse.builder()
                        .showTimeId(s.getId())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .roomId(s.getRoom().getId())
                        .roomType(s.getRoom().getRoomType().getName())
                        .roomName(s.getRoom().getName())
                        .totalSeat((long) s.getRoom().getSeats().size())
                        .totalSeatAvailable((long) countTotalSeatAvailable(s))
                        .build())
                .toList();
    }

    @Override
    public List<BookingSeatsResponse> getSeatsByShowTimeId(Long showTimeId) {
        ShowTime showTime = showTimeRepository.findByIdFetchAll(showTimeId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));
        
        if (Boolean.TRUE.equals(showTime.getIsDeleted())) {
            throw new AppException(ErrorCode.SHOWTIME_NOT_FOUND);
        }

        List<TicketResponse> tickets = showTime.getTickets().stream()
                .map(ticket -> TicketResponse.builder()
                        .ticketId(ticket.getId())
                        .seatStatus(ticket.getStatus().name())
                        .seatType(ticket.getSeat().getSeatType().getName())
                        .rowIdx(Integer.parseInt(ticket.getSeat().getRow()) - 1)
                        .columnInx(Integer.parseInt(ticket.getSeat().getColumn()) - 1)
                        .ticketPrice(ticket.getTicketPrice().getPrice())
                        .build())
                .collect(Collectors.toList());

        // 2. get seat held in redis
        Set<String> keys = redisService.findKeys("seatHold:showtime:" + showTimeId + ":*");
        if (!keys.isEmpty()) {
            Set<Long> heldSeatIds = keys.stream()
                    .map(k -> redisService.get(k, SeatHold.class))
                    .filter(Objects::nonNull)
                    .flatMap(h -> h.getSeats().stream().map(SeatTicketDTO::getTicketId))
                    .collect(Collectors.toSet());

            // 3.update held ticket for seats are get in db
            for (TicketResponse t : tickets) {
                if (heldSeatIds.contains(t.getTicketId()) && !"BOOKED".equals(t.getSeatStatus())) {
                    t.setSeatStatus("HELD");
                }
            }
        }

        return List.of(BookingSeatsResponse.builder()
                .showTimeId(showTime.getId())
                .roomId(showTime.getRoom().getId())
                .ticketResponses(tickets)
                .build());
    }

    @Override

    public void handleSeatAction(SeatSelectRequest req) {
        switch (req.getAction()) {
            case SELECT_SEAT -> handleSelectSeat(req);
            case DESELECT_SEAT -> handleDeselectSeat(req);
            default -> throw new IllegalArgumentException("Invalid seat action");
        }
    }

    private void handleSelectSeat(SeatSelectRequest req) {

        // hold seat to redis
        SeatHold seatHold = seatHoldService.holdSeats(req);

        if (seatHold == null) {
            log.warn("[BOOKING] User {} failed to hold seats", req.getUserId());
            broadcast(req, "FAILED");
        }

        // synchronize data to order session
        OrderSessionRequest sessionReq = new OrderSessionRequest();
        sessionReq.setUserId(req.getUserId());
        sessionReq.setShowtimeId(req.getShowtimeId());
        sessionReq.setTicketIds(seatHold.getSeats().stream().map(SeatTicketDTO::getTicketId).toList());
        orderSessionService.createOrUpdate(sessionReq);

        // Broadcast HELD
        broadcast(req, TicketStatus.HELD.name());
    }

    private void handleDeselectSeat(SeatSelectRequest req) {
        //release seat
        SeatHold updated = seatHoldService.releaseSeats(req);

        if (updated == null) {
            orderSessionService.delete(req.getUserId(), req.getShowtimeId());
            broadcast(req, TicketStatus.RELEASED.name());
            return;
        }

        //update order session
        OrderSessionRequest sessionReq = new OrderSessionRequest();

        sessionReq.setUserId(req.getUserId());
        sessionReq.setShowtimeId(req.getShowtimeId());
        sessionReq.setTicketIds(updated.getSeats().stream().map(SeatTicketDTO::getTicketId).toList());

        orderSessionService.removeTickets(sessionReq);

        // Broadcast seat release to all clients
        broadcast(req, TicketStatus.RELEASED.name());
    }


    private void broadcast(SeatSelectRequest req, String status) {
        List<SeatTicketDTO> seatDetails = req.getTicketIds().stream()
                .map(ticketId -> {
                    var ticketOpt = ticketRepository.findByIdWithSeat(ticketId);
                    if (ticketOpt.isEmpty()) {
                        return SeatTicketDTO.builder()
                                .ticketId(ticketId)
                                .status(status)
                                .build();
                    }
                    var ticket = ticketOpt.get();
                    return SeatTicketDTO.builder()
                            .ticketId(ticketId)
                            .rowIdx(Integer.parseInt(ticket.getSeat().getRow()) - 1)
                            .columnIdx(Integer.parseInt(ticket.getSeat().getColumn()) - 1)
                            .seatType(ticket.getSeat().getSeatType().getName())
                            .status(status)
                            .build();
                })
                .toList();

        messagingTemplate.convertAndSend(
                "/topic/seat/" + req.getShowtimeId(),
                Map.of("seats", seatDetails,
                        "status", status,
                        "userId", req.getUserId(),
                        "showtimeId", req.getShowtimeId())
        );
    }

    @Override
    public void broadcastBooked(Long showtimeId, List<Long> ticketIds) {
        log.info("[BOOKING] Broadcasting BOOKED status for showtime {} with {} tickets", showtimeId, ticketIds.size());
        
        List<SeatTicketDTO> seatDetails = ticketIds.stream()
                .map(ticketId -> {
                    var ticketOpt = ticketRepository.findByIdWithSeat(ticketId);
                    if (ticketOpt.isEmpty()) {
                        return SeatTicketDTO.builder()
                                .ticketId(ticketId)
                                .status(TicketStatus.BOOKED.name())
                                .build();
                    }
                    var ticket = ticketOpt.get();
                    return SeatTicketDTO.builder()
                            .ticketId(ticketId)
                            .rowIdx(Integer.parseInt(ticket.getSeat().getRow()) - 1)
                            .columnIdx(Integer.parseInt(ticket.getSeat().getColumn()) - 1)
                            .seatType(ticket.getSeat().getSeatType().getName())
                            .status(TicketStatus.BOOKED.name())
                            .build();
                })
                .toList();

        messagingTemplate.convertAndSend(
                "/topic/seat/" + showtimeId,
                Map.of("seats", seatDetails,
                        "status", TicketStatus.BOOKED.name(),
                        "showtimeId", showtimeId)
        );
        
        log.info("[BOOKING] Successfully broadcasted BOOKED status for {} tickets", seatDetails.size());
    }

    /**
     * Count available seats based on ticket status in the showtime
     * Only tickets with status AVAILABLE are counted (not HELD, BOOKED, or BLOCKED)
     * Uses TicketRepository to count directly from database for better performance
     */
    private int countTotalSeatAvailable(ShowTime showTime) {
        Long count = ticketRepository.countByShowTime_IdAndStatus(
                showTime.getId(), 
                TicketStatus.AVAILABLE
        );
        return count != null ? count.intValue() : 0;
    }

    @Override
    public List<TicketDetailResponse> getTicketDetailsByIds(List<Long> ids) {
        List<Ticket> tickets = ticketRepository.findTicketsWithRelations(ids);
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");


        return tickets.stream()
                .map(t -> {
                    Seat seat = t.getSeat();
                    ShowTime sh = t.getShowTime();
                    Room room = seat.getRoom();

                    String seatCode;
                    if (seat.getRow() != null && seat.getColumn() != null) {
                        int rowIndex1Based = Integer.parseInt(seat.getRow()); // 1,2,3,...
                        int rowIndex0Based = Math.max(0, rowIndex1Based - 1); // 0=A, 1=B, ...
                        char rowLetter = (char) ('A' + rowIndex0Based);
                        seatCode = rowLetter + String.valueOf(seat.getColumn());
                    } else {
                        seatCode = "";
                    }

                    return TicketDetailResponse.builder()
                            .ticketId(t.getId())
                            .seatCode(seatCode)
                            .seatType(seat.getSeatType().getName())
                            .ticketPrice(t.getTicketPrice().getPrice())

                            .roomName(room.getName())
                            .roomType(room.getRoomType().getName())
                            .hall(room.getName()) // hoặc format khác nếu bạn muốn

                            .showtimeId(sh.getId())
                            .showDate(sh.getStartTime().format(dateFmt))
                            .showTime(sh.getStartTime().format(timeFmt))

                            .movieName(sh.getMovie().getName())
                            .posterUrl(sh.getMovie().getPosterUrl())
                            .build();
                })
                .toList();

    }

    @Override
    public List<String> getDistinctMethodNames() {
        return paymentMethodRepository.findDistinctMethodNames();
    }

    @Override
    public List<String> getDistinctAllMethodNames() {
        return paymentMethodRepository.findDistinctAllMethodNames();
    }

    // Find all banks by methodName
    @Override
    public List<PaymentMethodDTO> getPaymentMethodsByName(String methodName) {
        List<PaymentMethod> methods = paymentMethodRepository.findByMethodName(methodName);
        return methods.stream()
                .map(m -> PaymentMethodDTO.builder()
                        .paymentName(m.getBankName() != null ? m.getBankName() : m.getMethodName())
                        .paymentCode(m.getPaymentCode())
                        .imageUrl(m.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }


}
