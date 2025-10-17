package vn.cineshow.dto.request.seat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatCellRequest {
    Long id;                       // null -> tạo mới

    @NotNull @Min(1)
    Integer rowIndex;              // 1..rows

    @NotNull @Min(1)
    Integer columnIndex;           // 1..columns

    @NotNull
    Long seatTypeId;

    @NotBlank                  // "ACTIVE" | "INACTIVE" | "BLOCKED"
    String status;

    Boolean isBlocked;         // true/false
    String note;               // có thể null
}
