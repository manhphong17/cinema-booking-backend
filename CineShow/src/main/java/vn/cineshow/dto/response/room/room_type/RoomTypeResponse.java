package vn.cineshow.dto.response.room.room_type;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomTypeResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean active;
}
