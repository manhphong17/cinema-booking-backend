package vn.cineshow.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.cineshow.dto.request.booking.SeatSelectRequest;
import vn.cineshow.dto.response.booking.SeatHold;
import vn.cineshow.dto.response.booking.SeatTicketDTO;
import vn.cineshow.enums.SeatShowTimeStatus;
import vn.cineshow.repository.TicketRepository;
import vn.cineshow.service.RedisService;
import vn.cineshow.service.SeatHoldService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatHoldServiceImpl implements SeatHoldService {
    private final RedisService redisService;
    private final TicketRepository ticketRepository;

    @Value("${booking.ttl.default}")
    long HOLD_DURATION;

    private String buildId(SeatSelectRequest req) {
        return String.format("seatHold:showtime:%d:user:%d", req.getShowtimeId(), req.getUserId());
    }

    /**
     * hold seat
     *
     * @param req
     */
    @Override
    public SeatHold holdSeats(SeatSelectRequest req) {
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
        }

        // loc cac ghe con trong
        List<Long> availableSeats = req.getTicketIds().stream()
                .filter(id -> !heldSeatIds.contains(id))
                .toList();

        if (availableSeats.isEmpty()) {
            return null;
        }

        // lay hold hien tai cua user trong redis
        SeatHold existingHold = redisService.get(key, SeatHold.class);
        final Set<Long> alreadyHeldTicketIds;

        if (existingHold != null && existingHold.getSeats() != null) {
            alreadyHeldTicketIds = existingHold.getSeats().stream()
                    .map(SeatTicketDTO::getTicketId)
                    .collect(java.util.stream.Collectors.toSet());
        } else {
            alreadyHeldTicketIds = new HashSet<>();
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
        }
        allSeats.addAll(newSeats);

        SeatHold hold = SeatHold.builder()
                .showtimeId(req.getShowtimeId())
                .userId(req.getUserId())
                .seats(allSeats)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(HOLD_DURATION))
                .build();

        // save redis voi TTL
        if (existingHold == null) {
            redisService.save(key, hold, HOLD_DURATION);
            log.info("[REDIS SAVE] User {} now holds {} seats in total", req.getUserId(), allSeats.size());
        } else {
            redisService.update(key, hold);
            log.info("[REDIS UPDATE] User {} now holds {} seats in total", req.getUserId(), allSeats.size());
        }

        return hold;
    }

    /**
     * release seat
     *
     * @param req
     */
    @Override
    public SeatHold releaseSeats(SeatSelectRequest req) {
        String key = buildId(req);

        // Get existing hold from Redis
        SeatHold existing = redisService.get(key, SeatHold.class);
        if (existing == null) {
            return null;
        }

        // Remove only the specified seats from the hold
        Set<Long> ticketsToRelease = new HashSet<>(req.getTicketIds());
        List<SeatTicketDTO> remainingSeats = existing.getSeats().stream()
                .filter(seat -> !ticketsToRelease.contains(seat.getTicketId()))
                .toList();

        if (remainingSeats.isEmpty()) {
            // No seats left, delete the entire key
            redisService.delete(key);
            return null;
        }
        // Update Redis with remaining seats
        SeatHold updatedHold = SeatHold.builder()
                .showtimeId(req.getShowtimeId())
                .userId(req.getUserId())
                .seats(remainingSeats)
                .createdAt(existing.getCreatedAt())
                .expiresAt(existing.getExpiresAt())
                .build();

        redisService.update(key, updatedHold);

        log.info("[REDIS UPDATE] User {} released {} seats, {} remaining",
                req.getUserId(), req.getTicketIds().size(), remainingSeats.size());
        return updatedHold;
    }

    @Override
    public long getExpire(Long showtimeId, Long userId) {
        String key = String.format("seatHold:showtime:%d:user:%d", showtimeId, userId);
        Long ttl = redisService.getTTL(key);

        if (ttl == null || ttl <= 0) {
            return 0L;
        }

        return ttl;
    }

    @Override
    public SeatHold getCurrentHold(Long showtimeId, Long userId) {
        String key = String.format("seatHold:showtime:%d:user:%d", showtimeId, userId);
        return redisService.get(key, SeatHold.class);
    }

}
