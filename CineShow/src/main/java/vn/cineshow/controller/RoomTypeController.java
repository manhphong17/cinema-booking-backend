package vn.cineshow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.room.room_type.RoomTypeDTO;
import vn.cineshow.service.RoomTypeService;

import java.util.List;

@RestController
@RequestMapping("/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    // GET /room-types
    // Response: [ { id, code, name, description? }, ... ]
    @GetMapping
    public ResponseData<List<RoomTypeDTO>> getRoomTypes() {
        List<RoomTypeDTO> roomTypes = roomTypeService.getAllRoomTypesDTO();
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy danh sách loại phòng thành công", roomTypes);
    }
}
