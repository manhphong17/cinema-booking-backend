package vn.cineshow.dto.response.booking;

import lombok.*;
import vn.cineshow.enums.SeatShowTimeStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatUpdateMessage {
    private Long showtimeId;
    private List<Long> tickets;
    private SeatShowTimeStatus status;
    private Long userId;
}
