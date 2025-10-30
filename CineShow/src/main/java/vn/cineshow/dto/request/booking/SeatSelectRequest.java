package vn.cineshow.dto.request.booking;

import lombok.Getter;
import lombok.ToString;
import vn.cineshow.enums.SeatAction;

import java.util.List;

@Getter
@ToString
public class SeatSelectRequest {

    private SeatAction action;
    private Long showtimeId;
    private Long userId;
    private List<Long> ticketIds;
}
