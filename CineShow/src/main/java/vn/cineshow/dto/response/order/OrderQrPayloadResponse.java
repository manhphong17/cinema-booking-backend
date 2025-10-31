package vn.cineshow.dto.response.order;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class OrderQrPayloadResponse {
    Long orderId;
    Long userId;
    LocalDateTime createdAt;
    Double totalPrice;
    String status;
    String orderCode;   // <— thêm field này

    String movieName;
    LocalDateTime showtimeStart;
    String roomName;
    List<String> seats;
    List<String> ticketCodes;
}
