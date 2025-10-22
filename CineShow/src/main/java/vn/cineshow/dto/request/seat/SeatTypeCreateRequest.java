package vn.cineshow.dto.request.seat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatTypeCreateRequest {
    @NotBlank
    @Size(max = 50)
    private String name;
    private String description;
    private Boolean active; // optional, default true
}
