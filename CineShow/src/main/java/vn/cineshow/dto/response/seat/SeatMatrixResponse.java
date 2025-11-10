// File: src/main/java/vn/cineshow/dto/response/seat/SeatMatrixResponse.java
package vn.cineshow.dto.response.seat;

import lombok.*;
import vn.cineshow.dto.response.room.RoomDTO;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatMatrixResponse {
    private RoomDTO room;
    private List<List<SeatCellDTO>> matrix; // may contain null if a seat is missing in DB
}
