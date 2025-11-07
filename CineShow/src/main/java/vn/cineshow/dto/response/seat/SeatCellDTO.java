package vn.cineshow.dto.response.seat;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.dto.response.seat.seat_type.SeatTypeDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatCellDTO {
    Long id;

    Integer rowIndex;
    Integer columnIndex;

    String rowLabel;
    Integer number;

    SeatTypeDTO seatType;
    String status;
    Boolean isBlocked;
    String note;
}
