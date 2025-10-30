package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.request.booking.SeatSelectRequest;
import vn.cineshow.dto.response.booking.SeatHold;
import vn.cineshow.dto.response.booking.SeatTicketDTO;
import vn.cineshow.enums.SeatShowTimeStatus;
import vn.cineshow.repository.SeatHoldRepository;
import vn.cineshow.repository.TicketRepository;
import vn.cineshow.service.RedisService;
import vn.cineshow.service.SeatHoldService;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatHoldServiceImpl implements SeatHoldService {
    private static final long HOLD_DURATION = (long) 5 * 60;// 5 minutes
    private final RedisService redisService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TicketRepository ticketRepository;
    private final SeatHoldRepository seatHoldRepository;

    private String buildId(SeatSelectRequest req) {
        return String.format("seatHold:showtime:%d:user:%d", req.getShowtimeId(), req.getUserId());
    }

    @Override
    public void processSeatAction(SeatSelectRequest req) {
        switch (req.getAction()) {
            case SELECT_SEAT -> holdSeats(req);
            case DESELECT_SEAT -> releaseSeats(req);
            default -> throw new IllegalStateException("Unexpected value: " + req.getAction());
        }
    }

    /**
     * hold seat
     *
     * @param req
     */
    private void holdSeats(SeatSelectRequest req) {
        String key = buildId(req);

        // validate + save redis
        String pattern = String.format("seatHold:showtime:%d:*", req.getShowtimeId());
        Set<String> allkeys = redisService.findKeys(pattern);

        // tao danh sach ghe dang bi nguoi khac giu
        Set<Long> heldSeatIds = new HashSet<>();
        for (String keys : allkeys) {
            if (keys.equals(key)) continue;
            SeatHold hold = redisService.get(keys, SeatHold.class);
            if (hold != null) {
                hold.getSeats().forEach(seat -> heldSeatIds.add(seat.getTicketId()));
            }
            log.debug("seats hold: " + hold);
        }

        // loc cac ghe con trong
        List<Long> availableSeats = req.getTicketIds().stream()
                .filter(id -> !heldSeatIds.contains(id))
                .toList();

        if (availableSeats.isEmpty()) {
            broadcast(req, "FAILED");
            log.warn("User {} failed to hold seats", req.getUserId());
            return;
        }

        // lay hold hien tai cua user trong redis
        SeatHold existingHold = redisService.get(key, SeatHold.class);
        final Set<Long> alreadyHeldTicketIds;

        if (existingHold != null && existingHold.getSeats() != null) {
            alreadyHeldTicketIds = existingHold.getSeats().stream()
                    .map(SeatTicketDTO::getTicketId)
                    .collect(java.util.stream.Collectors.toSet());
            log.info("[MERGE] User {} already holds {} seats: {}", req.getUserId(), alreadyHeldTicketIds.size(), alreadyHeldTicketIds);
        } else {
            alreadyHeldTicketIds = new HashSet<>();
            log.info("[MERGE] User {} has no existing holds", req.getUserId());
        }

        // Build list of NEW seats to add
        List<SeatTicketDTO> newSeats = availableSeats.stream()
                .filter(ticketId -> !alreadyHeldTicketIds.contains(ticketId)) // chi add ghe moi
                .map(ticketId -> {
                    var ticket = ticketRepository.findByIdWithSeat(ticketId).orElseThrow();
                    return SeatTicketDTO.builder()
                            .ticketId(ticketId)
                            .rowIdx(Integer.parseInt(ticket.getSeat().getRow()) - 1)
                            .columnIdx(Integer.parseInt(ticket.getSeat().getColumn()) - 1)
                            .seatType(ticket.getSeat().getSeatType().getName())
                            .status(SeatShowTimeStatus.HELD.name())
                            .build();
                })
                .toList();

        // Merge old + new seats
        List<SeatTicketDTO> allSeats = new ArrayList<>();
        if (existingHold != null && existingHold.getSeats() != null) {
            allSeats.addAll(existingHold.getSeats());
            log.info("[MERGE] Keeping {} existing seats", existingHold.getSeats().size());
        }
        allSeats.addAll(newSeats);
        log.info("[MERGE] Adding {} new seats, total now: {}", newSeats.size(), allSeats.size());

        SeatHold hold = SeatHold.builder()
                .showtimeId(req.getShowtimeId())
                .userId(req.getUserId())
                .seats(allSeats)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(HOLD_DURATION))
                .build();

        // save redis voi TTL
        redisService.save(key, hold, HOLD_DURATION);
        log.info("[REDIS SAVE] User {} now holds {} seats in total", req.getUserId(), allSeats.size());

        // log tat ca ticket id
        List<Long> allTicketIds = allSeats.stream().map(SeatTicketDTO::getTicketId).toList();
        log.info("[REDIS SAVE] Ticket IDs: {}", allTicketIds);

        broadcast(req, SeatShowTimeStatus.HELD.name());
    }

    /**
     * release seat
     *
     * @param req
     */
    private void releaseSeats(SeatSelectRequest req) {
        String key = buildId(req);

        // Get existing hold from Redis
        SeatHold existing = redisService.get(key, SeatHold.class);
        if (existing == null) {
            log.debug("No held seats found for user {} at showtime {}", req.getUserId(), req.getShowtimeId());
            return;
        }

        // Remove only the specified seats from the hold
        Set<Long> ticketsToRelease = new HashSet<>(req.getTicketIds());
        List<SeatTicketDTO> remainingSeats = existing.getSeats().stream()
                .filter(seat -> !ticketsToRelease.contains(seat.getTicketId()))
                .toList();

        if (remainingSeats.isEmpty()) {
            // No seats left, delete the entire key
            redisService.delete(key);
            log.info("User {} released all seats for showtime {}", req.getUserId(), req.getShowtimeId());
        } else {
            // Update Redis with remaining seats
            SeatHold updatedHold = SeatHold.builder()
                    .showtimeId(req.getShowtimeId())
                    .userId(req.getUserId())
                    .seats(remainingSeats)
                    .createdAt(existing.getCreatedAt())
                    .expiresAt(existing.getExpiresAt())
                    .build();

            redisService.save(key, updatedHold, HOLD_DURATION);
            log.info("User {} released {} seats, {} seats remaining",
                    req.getUserId(), req.getTicketIds().size(), remainingSeats.size());
        }

        // Broadcast seat release to all clients
        broadcast(req, SeatShowTimeStatus.RELEASED.name());
    }


    /*    private void holdSeats(SeatSelectRequest req) {
            String id = buildId(req);

            SeatHold seatHold = SeatHold.builder()
                    .id(id)
                    .userId(req.getUserId())
                    .showtimeId(req.getShowtimeId())
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusSeconds(HOLD_DURATION))
                    .ttl(HOLD_DURATION)
                    .seats(buildSeatDTOs(req))
                    .build();

            seatHoldRepository.save(seatHold);
            broadcast(req, SeatShowTimeStatus.HELD.name());
        }*/
    private List<SeatTicketDTO> buildSeatDTOs(SeatSelectRequest req) {

        return req.getTicketIds().stream().map(ticketId -> {
            var ticket = ticketRepository.findByIdWithSeat(ticketId).orElseThrow();
            return SeatTicketDTO.builder()
                    .ticketId(ticketId)
                    .rowIdx(Integer.parseInt(ticket.getSeat().getRow()) - 1)
                    .columnIdx(Integer.parseInt(ticket.getSeat().getColumn()) - 1)
                    .status(SeatShowTimeStatus.HELD.name())
                    .seatType(ticket.getSeat().getSeatType().getName())
                    .build();
        }).toList();
    }



    /*private void releaseSeats(SeatSelectRequest req) {
        List<SeatHold> holds = new ArrayList<>();
        seatHoldRepository.findAll().forEach(holds::add);

        Optional<SeatHold> existingOpt = holds.stream()
                .filter(h -> h.getUserId().equals(req.getUserId()) && h.getShowtimeId().equals(req.getShowtimeId()))
                .findFirst();

        if (existingOpt.isEmpty()) {
            log.debug("No held seats found for user {} at showtime {}", req.getUserId(), req.getShowtimeId());
            return;
        }

        SeatHold existing = existingOpt.get();
        Set<Long> toRelease = new HashSet<>(req.getTicketIds());
        List<SeatTicketDTO> remaining = existing.getSeats().stream()
                .filter(s -> !toRelease.contains(s.getTicketId()))
                .toList();

        if (remaining.isEmpty()) {
            seatHoldRepository.delete(existing);
            log.info("User {} released all seats for showtime {}", req.getUserId(), req.getShowtimeId());
        } else {
            existing.setSeats(remaining);
            seatHoldRepository.save(existing);
            log.info("User {} released {} seats, {} remaining", req.getUserId(), req.getTicketIds().size(), remaining.size());
        }

        broadcast(req, SeatShowTimeStatus.RELEASED.name());
    }*/


    @Override
    public SeatHold getHeldSeatsByUser(Long showtimeId, Long userId) {
        String key = String.format("seatHold:showtime:%d:user:%d", showtimeId, userId);
        return redisService.get(key, SeatHold.class);
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

}
