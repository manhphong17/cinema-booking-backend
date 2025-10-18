package vn.cineshow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.seat.seat_type.SeatTypeDTO;
import vn.cineshow.service.SeatTypeService;

import java.util.List;

@RestController
@RequestMapping("/seat-types")
@RequiredArgsConstructor
public class SeatTypeController {

    private final SeatTypeService seatTypeService;

    // GET /seat-types
    // Response: [ { id, code, name, description? }, ... ]
    @GetMapping("/seat-types")
    public ResponseData<List<SeatTypeDTO>> getSeatTypes() {
        List<SeatTypeDTO> seatTypes = seatTypeService.getAllSeatTypesDTO();
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy danh sách loại ghế thành công", seatTypes);
    }
}
