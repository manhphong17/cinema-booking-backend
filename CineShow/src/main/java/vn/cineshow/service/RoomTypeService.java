package vn.cineshow.service;

import vn.cineshow.dto.request.room.RoomTypeCreateRequest;
import vn.cineshow.dto.request.room.RoomTypeUpdateRequest;
import vn.cineshow.dto.response.room.room_type.RoomTypeDTO;
import vn.cineshow.dto.response.room.room_type.RoomTypeResponse;

import java.util.List;

public interface RoomTypeService {
    /**
     * Lấy toàn bộ danh sách loại phòng chiếu dưới dạng DTO.
     */
    List<RoomTypeDTO> getAllRoomTypesDTO();

    List<RoomTypeResponse> findAll(Boolean onlyActive);
    RoomTypeResponse findById(Long id);
    RoomTypeResponse create(RoomTypeCreateRequest req);
    RoomTypeResponse update(Long id, RoomTypeUpdateRequest req); // PUT
    RoomTypeResponse patch(Long id, RoomTypeUpdateRequest req);  // PATCH
    void delete(Long id);
    RoomTypeResponse activate(Long id);
    RoomTypeResponse deactivate(Long id);
}
