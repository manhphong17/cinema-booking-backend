package vn.cineshow.dto.request.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomTypeCreateRequest {
    @NotBlank
    @Size(max = 50)
    private String name;
    private String description;
    private Boolean active; // optional, default true
}
