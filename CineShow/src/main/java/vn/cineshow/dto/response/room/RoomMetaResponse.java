package vn.cineshow.dto.response.room;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.dto.response.room.room_type.RoomTypeDTO;
import vn.cineshow.dto.response.seat.seat_type.SeatTypeDTO;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomMetaResponse {
    List<RoomTypeDTO> roomTypes;
    List<SeatTypeDTO> seatTypes;
}
