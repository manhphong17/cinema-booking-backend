// File: src/main/java/vn/cineshow/dto/request/seat/SeatCellRequest.java

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
    Long id;

    @NotNull @Min(1)
    Integer rowIndex;

    @NotNull @Min(1)
    Integer columnIndex;

    @NotNull
    Long seatTypeId;

    @NotBlank
    String status;

}
