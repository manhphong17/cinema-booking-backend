package vn.cineshow.dto.response.seat;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.dto.response.room.RoomDTO;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatMatrixResponse {
    RoomDTO room;
    List<List<SeatCellDTO>> matrix;
}
