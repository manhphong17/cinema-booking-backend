package vn.cineshow.dto.response.order;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class OrderListItemResponse {
    Long orderId;
    LocalDateTime createdAt;
    String movieName;
    LocalDateTime showtimeStart;
    String code;
    String roomName;
    List<String> seats; // "I7","I8"...
    Double totalPrice;
    String status;
}
