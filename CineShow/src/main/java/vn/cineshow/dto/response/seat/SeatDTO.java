package vn.cineshow.dto.response.seat;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.dto.response.seat.seat_type.SeatTypeDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatDTO {
    Long id;

    // Vị trí ghế theo ma trận (1-based index)
    Integer row;        // hàng (VD: 1..N)
    Integer column;     // cột  (VD: 1..M)

    // Loại ghế hiển thị trên UI (STANDARD, VIP, ...)
    SeatTypeDTO seatType;

    // Trạng thái ghế: "ACTIVE" | "INACTIVE" | "BLOCKED"
    // Nếu bạn dùng enum SeatStatus trong project, đổi kiểu String -> SeatStatus
    String status;
}
