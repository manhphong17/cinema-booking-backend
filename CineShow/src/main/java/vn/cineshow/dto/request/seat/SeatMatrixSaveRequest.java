// File: src/main/java/vn/cineshow/dto/request/seat/SeatMatrixSaveRequest.java
package vn.cineshow.dto.request.seat;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatMatrixSaveRequest {
    @NotNull
    private List<List<SeatCellRequest>> matrix; // full grid (no walkway)
}
