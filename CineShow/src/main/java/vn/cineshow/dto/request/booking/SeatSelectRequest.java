package vn.cineshow.dto.request.booking;

import vn.cineshow.enums.SeatAction;

import java.util.List;

public class SeatSelectRequest {

    private SeatAction action;
    private Long showtimeId;
    private Long userId;
    private List<Long> ticketIds;
}
