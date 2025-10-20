package vn.cineshow.service;

import vn.cineshow.dto.request.seat.BulkBlockRequest;
import vn.cineshow.dto.request.seat.BulkTypeRequest;
import vn.cineshow.dto.request.seat.SeatInitRequest;
import vn.cineshow.dto.request.seat.SeatMatrixRequest;
import vn.cineshow.dto.response.seat.SeatMatrixResponse;

import java.util.Map;

public interface SeatService {

    /**
     * Khởi tạo toàn bộ ghế của phòng theo kích thước và loại ghế mặc định.
     * @return số ghế đã tạo
     */
    int initSeats(Long roomId, SeatInitRequest request);

    /**
     * Lấy ma trận ghế của phòng (room + matrix).
     * @return SeatMatrixResponse hoặc null nếu không tìm thấy room
     */
    SeatMatrixResponse getSeatMatrix(Long roomId);

    /**
     * Lưu (upsert) toàn bộ ma trận ghế gửi từ FE.
     * Tự động tạo mới / cập nhật / xóa ghế thừa.
     * Đồng bộ rows, columns, capacity của Room.
     * @return map {updated, created, deleted}
     */
    Map<String, Integer> saveSeatMatrix(Long roomId, SeatMatrixRequest request);

    /**
     * Đổi loại ghế hàng loạt theo toạ độ.
     * @return số ghế bị tác động
     */
    int bulkUpdateSeatType(Long roomId, BulkTypeRequest request);

    /**
     * Khóa/Mở khóa ghế hàng loạt theo toạ độ.
     * @return số ghế bị tác động
     */
    int bulkBlockSeats(Long roomId, BulkBlockRequest request);
}
