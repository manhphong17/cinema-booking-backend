package vn.cineshow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.seat.BulkBlockRequest;
import vn.cineshow.dto.request.seat.BulkTypeRequest;
import vn.cineshow.dto.request.seat.SeatInitRequest;
import vn.cineshow.dto.request.seat.SeatMatrixRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.seat.SeatMatrixResponse;
import vn.cineshow.service.SeatService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/rooms/{roomId}/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    // POST /rooms/{roomId}/seats/init
    // Body: { rows, columns, defaultSeatTypeId } -> { created }
    @PostMapping("/init")
    public ResponseData<Map<String, Integer>> initSeats(@PathVariable Long roomId,
                                                        @RequestBody @Valid SeatInitRequest request) {
        int created = seatService.initSeats(roomId, request);
        Map<String, Integer> data = new HashMap<>();
        data.put("created", created);
        return new ResponseData<>(HttpStatus.OK.value(), "Khởi tạo ghế thành công", data);
    }

    // GET /rooms/{roomId}/seats/matrix
    // Trả: SeatMatrixResponse { room:{...}, matrix:[ [SeatCellDTO] ] }
    @GetMapping("/matrix")
    public ResponseData<SeatMatrixResponse> getSeatMatrix(@PathVariable Long roomId) {
        SeatMatrixResponse matrix = seatService.getSeatMatrix(roomId);
        if (matrix == null) {
            return new ResponseData<>(HttpStatus.NOT_FOUND.value(), "Không tìm thấy phòng hoặc ma trận ghế", null);
        }
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy ma trận ghế thành công", matrix);
    }

    // PUT /rooms/{roomId}/seats/matrix (bulk save)
    // Body: SeatMatrixRequest { matrix: [[SeatCellRequest]] } -> { updated, created, deleted }
    @PutMapping("/matrix")
    public ResponseData<Map<String, Integer>> saveSeatMatrix(@PathVariable Long roomId,
                                                             @RequestBody @Valid SeatMatrixRequest request) {
        Map<String, Integer> result = seatService.saveSeatMatrix(roomId, request);
        return new ResponseData<>(HttpStatus.OK.value(), "Lưu cấu hình ghế thành công", result);
    }

    // PATCH /rooms/{roomId}/seats/bulk-type
    // Body: { targets:[{rowIndex,columnIndex}...], seatTypeId } -> { affected }
    @PatchMapping("/bulk-type")
    public ResponseData<Map<String, Integer>> bulkUpdateSeatType(@PathVariable Long roomId,
                                                                 @RequestBody @Valid BulkTypeRequest request) {
        int affected = seatService.bulkUpdateSeatType(roomId, request);
        Map<String, Integer> data = new HashMap<>();
        data.put("affected", affected);
        return new ResponseData<>(HttpStatus.OK.value(), "Cập nhật loại ghế hàng loạt thành công", data);
    }

    // PATCH /rooms/{roomId}/seats/bulk-block
    // Body: { targets:[{rowIndex,columnIndex}...], blocked:true|false } -> { affected }
    @PatchMapping("/bulk-block")
    public ResponseData<Map<String, Integer>> bulkBlockSeats(@PathVariable Long roomId,
                                                             @RequestBody @Valid BulkBlockRequest request) {
        int affected = seatService.bulkBlockSeats(roomId, request);
        Map<String, Integer> data = new HashMap<>();
        data.put("affected", affected);
        return new ResponseData<>(HttpStatus.OK.value(), "Cập nhật trạng thái khóa ghế hàng loạt thành công", data);
    }
}
