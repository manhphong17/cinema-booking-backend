package vn.cineshow.dto.request.booking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import vn.cineshow.enums.SeatAction;

import java.util.List;

@Getter
@ToString
public class SeatSelectRequest {

    @NotNull
    private SeatAction action;
    @NotNull
    @Min(1)
    private Long showtimeId;
    @NotNull
    @Min(1)
    private Long userId;
    @NotNull
    private List<Long> ticketIds;
}
