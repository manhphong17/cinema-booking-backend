package vn.cineshow.service;

import vn.cineshow.dto.request.booking.SeatSelectRequest;
import vn.cineshow.dto.response.booking.SeatHold;

public interface SeatHoldService {
    SeatHold holdSeats(SeatSelectRequest req);

    SeatHold releaseSeats(SeatSelectRequest req);

    long getExpire(Long showtimeId, Long userId);
    
    SeatHold getCurrentHold(Long showtimeId, Long userId);
}
