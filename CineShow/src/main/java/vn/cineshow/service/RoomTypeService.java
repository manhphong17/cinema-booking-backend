package vn.cineshow.service;

import vn.cineshow.dto.response.room.room_type.RoomTypeDTO;

import java.util.List;

public interface RoomTypeService {
    /**
     * Lấy toàn bộ danh sách loại phòng chiếu dưới dạng DTO.
     */
    List<RoomTypeDTO> getAllRoomTypesDTO();
}
