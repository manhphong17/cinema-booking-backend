package vn.cineshow.dto.response.order;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderListItemResponse {
    Long orderId;
    LocalDateTime createdAt;
    String userName;
    String movieName;
    LocalDateTime showtimeStart;
    String code;
    String roomName;
    List<String> seats; // "I7","I8"...
    List<OrderConcessionItem> concessions;
    Double totalPrice;
    String status;
}
