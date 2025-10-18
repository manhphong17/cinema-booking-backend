package vn.cineshow.dto.request.seat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatPosition {
    @NotNull @Min(1)
    Integer rowIndex;

    @NotNull @Min(1)
    Integer columnIndex;
}
