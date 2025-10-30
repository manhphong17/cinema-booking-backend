package vn.cineshow.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.response.booking.*;
import vn.cineshow.enums.SeatStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.Room;
import vn.cineshow.model.ShowTime;
import vn.cineshow.repository.MovieRepository;
import vn.cineshow.repository.ShowTimeRepository;
import vn.cineshow.service.BookingService;
import vn.cineshow.service.RedisService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j(topic = "BOOKING-SERVICE")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl implements BookingService {
    MovieRepository movieRepository;
    ShowTimeRepository showTimeRepository;
    RedisService redisService;

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
        return showTimeRepository.findByMovie_IdAndStartTime(movieId, startTime).stream()
                .filter(showTime -> countTotalSeatAvailable(showTime.getRoom()) > 0)
                .map(s -> ShowTimeResponse.builder()
                        .showTimeId(s.getId())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .roomId(s.getRoom().getId())
                        .roomType(s.getRoom().getRoomType().getName())
                        .roomName(s.getRoom().getName())
                        .totalSeat((long) s.getRoom().getSeats().size())
                        .totalSeatAvailable((long) countTotalSeatAvailable(s.getRoom()))
                        .build())
                .toList();
    }

    @Override
    public List<BookingSeatsResponse> getSeatsByShowTimeId(Long showTimeId) {
        // 1.get Show time and ticket list in DB
        ShowTime showTime = showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));

        List<TicketResponse> tickets = showTime.getTickets().stream()
                .map(ticket -> TicketResponse.builder()
                        .ticketId(ticket.getId())
                        .seatStatus(ticket.getStatus().name())
                        .seatType(ticket.getSeat().getSeatType().getName())
                        .rowIdx(Integer.parseInt(ticket.getSeat().getRow()) - 1)
                        .columnInx(Integer.parseInt(ticket.getSeat().getColumn()) - 1)
                        .ticketPrice(ticket.getPrice())
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


    private int countTotalSeatAvailable(Room room) {
        return room.getSeats().stream().filter(seat -> seat.getStatus().equals(SeatStatus.AVAILABLE)).toList().size();
    }
}
