package vn.cineshow.dto.request.seat;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatTypeUpdateRequest {
    @Size(max = 50)
    private String name;
    private String description;
    private Boolean active;
}
