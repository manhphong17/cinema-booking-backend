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

    Integer rowIndex;     // 1..rows
    Integer columnIndex;  // 1..columns

    String rowLabel;      // "A", "B", ...
    Integer number;       // 1..N (số ghế trong hàng)

    SeatTypeDTO seatType; // {id, code, name, description?}
    String status;        // "ACTIVE" | "INACTIVE" | "BLOCKED"
    Boolean isBlocked;    // true/false
    String note;          // có thể null
}
