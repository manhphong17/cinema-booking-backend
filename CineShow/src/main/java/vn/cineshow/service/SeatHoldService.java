package vn.cineshow.service;

import vn.cineshow.dto.request.booking.SeatSelectRequest;
import vn.cineshow.dto.response.booking.SeatHold;

public interface SeatHoldService {
    void processSeatAction(SeatSelectRequest req);

    SeatHold getHeldSeatsByUser(Long showtimeId, Long userId);
}
