package vn.cineshow.service;

import vn.cineshow.dto.response.seat.seat_type.SeatTypeDTO;

import java.util.List;

public interface SeatTypeService {
    /**
     * Lấy toàn bộ danh sách loại ghế dưới dạng DTO.
     * @return Danh sách SeatTypeDTO
     */
    List<SeatTypeDTO> getAllSeatTypesDTO();
}
