package vn.cineshow.dto.request.booking;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcessionListRequest {
    @NotNull
    private Long showtimeId;

    @NotNull
    private Long userId;

    private List<ConcessionOrderRequest> concessions;
}
