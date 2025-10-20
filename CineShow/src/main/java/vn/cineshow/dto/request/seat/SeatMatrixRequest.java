package vn.cineshow.dto.request.seat;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatMatrixRequest {
    @NotNull
    List<List<SeatCellRequest>> matrix;
}
