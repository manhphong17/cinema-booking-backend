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
public class SeatInitRequest {
    @NotNull @Min(1)
    Integer rows;

    @NotNull @Min(1)
    Integer columns;

    @NotNull
    Long defaultSeatTypeId;
}
