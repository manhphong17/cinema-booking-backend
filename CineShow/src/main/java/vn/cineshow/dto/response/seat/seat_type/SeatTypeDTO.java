package vn.cineshow.dto.response.seat.seat_type;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatTypeDTO {
    Long id;
    String code;
    String name;
    String description; // có thể null
}
