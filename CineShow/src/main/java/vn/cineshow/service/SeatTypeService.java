package vn.cineshow.service;

import vn.cineshow.dto.request.seat.SeatTypeCreateRequest;
import vn.cineshow.dto.request.seat.SeatTypeUpdateRequest;
import vn.cineshow.dto.response.seat.seat_type.SeatTypeDTO;
import vn.cineshow.dto.response.seat.seat_type.SeatTypeResponse;

import java.util.List;

public interface SeatTypeService {
    /**
     * Lấy toàn bộ danh sách loại ghế dưới dạng DTO.
     * @return Danh sách SeatTypeDTO
     */
    List<SeatTypeDTO> getAllSeatTypesDTO();


    List<SeatTypeResponse> findAll(Boolean onlyActive);
    SeatTypeResponse findById(Long id);
    SeatTypeResponse create(SeatTypeCreateRequest req);
    SeatTypeResponse update(Long id, SeatTypeUpdateRequest req);
    SeatTypeResponse patch(Long id, SeatTypeUpdateRequest req);
    void delete(Long id);
    SeatTypeResponse activate(Long id);
    SeatTypeResponse deactivate(Long id);
}
