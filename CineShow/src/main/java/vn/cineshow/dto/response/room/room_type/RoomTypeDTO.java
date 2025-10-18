package vn.cineshow.dto.response.room.room_type;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomTypeDTO {
    Long id;
    String code;
    String name;
    String description; // có thể null
}
