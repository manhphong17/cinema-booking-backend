package vn.cineshow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.room.RoomRequest;
import vn.cineshow.dto.response.PageResponse;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.room.RoomDTO;
import vn.cineshow.dto.response.room.RoomMetaResponse;
import vn.cineshow.dto.response.room.room_type.RoomTypeDTO;
import vn.cineshow.dto.response.seat.seat_type.SeatTypeDTO;
import vn.cineshow.service.RoomService;
import vn.cineshow.service.RoomTypeService;
import vn.cineshow.service.SeatTypeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomTypeService roomTypeService;
    private final SeatTypeService seatTypeService;

    @GetMapping("/meta")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseData<RoomMetaResponse> getMeta() {
        var meta = RoomMetaResponse.builder()
                .roomTypes(roomTypeService.getAllRoomTypesDTO())
                .seatTypes(seatTypeService.getAllSeatTypesDTO())
                .build();
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy metadata thành công", meta);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseData<PageResponse<List<RoomDTO>>> searchRooms(
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy
    ) {
        PageResponse<List<RoomDTO>> page = roomService.searchRooms(pageNo, pageSize, keyword, roomTypeId, status, sortBy);
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy danh sách phòng chiếu thành công", page);
    }

    @GetMapping("/{roomId}")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseData<RoomDTO> getRoomDetail(@PathVariable Long roomId) {
        RoomDTO room = roomService.getRoomDetail(roomId);
        if (room == null) {
            return new ResponseData<>(HttpStatus.NOT_FOUND.value(), "Không tìm thấy phòng chiếu", null);
        }
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy thông tin phòng chiếu thành công", room);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseData<RoomDTO> createRoom(@RequestBody @Valid RoomRequest request) {
        RoomDTO created = roomService.createRoom(request);
        return new ResponseData<>(HttpStatus.CREATED.value(), "Đã thêm phòng chiếu thành công", created);
    }

    @PutMapping("/{roomId}")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseData<RoomDTO> updateRoom(@PathVariable Long roomId,
                                            @RequestBody @Valid RoomRequest request) {
        RoomDTO updated = roomService.updateRoom(roomId, request);
        if (updated == null) {
            return new ResponseData<>(HttpStatus.NOT_FOUND.value(), "Không tìm thấy phòng chiếu để cập nhật", null);
        }
        return new ResponseData<>(HttpStatus.OK.value(), "Đã cập nhật phòng chiếu thành công", updated);
    }

    @DeleteMapping("/{roomId}")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseData<?> deleteRoom(@PathVariable Long roomId) {
        boolean deleted = roomService.deleteRoom(roomId);
        if (!deleted) {
            return new ResponseData<>(HttpStatus.NOT_FOUND.value(), "Không tìm thấy phòng chiếu để xóa");
        }
        return new ResponseData<>(HttpStatus.OK.value(), "Đã xóa phòng chiếu thành công");
    }
}