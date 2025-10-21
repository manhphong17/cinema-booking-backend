package vn.cineshow.service;

import vn.cineshow.dto.request.room.RoomRequest;
import vn.cineshow.dto.response.PageResponse;
import vn.cineshow.dto.response.room.RoomDTO;

import java.util.List;

public interface RoomService {

    PageResponse<List<RoomDTO>> searchRooms(Integer pageNo,
                                            Integer pageSize,
                                            String keyword,
                                            Long roomTypeId,
                                            String status,
                                            String sortBy);

    RoomDTO getRoomDetail(Long roomId);

    RoomDTO createRoom(RoomRequest request);

    RoomDTO updateRoom(Long roomId, RoomRequest request);

    boolean deleteRoom(Long roomId);
}
