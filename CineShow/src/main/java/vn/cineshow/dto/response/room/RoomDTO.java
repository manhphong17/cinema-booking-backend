package vn.cineshow.dto.response.room;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.dto.response.room.room_type.RoomTypeDTO;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomDTO {
    Long id;
    String name;
    RoomTypeDTO roomType;

    Integer rows;
    Integer columns;
    Integer capacity;

    String status;      // "ACTIVE" | "INACTIVE"
    String description;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
