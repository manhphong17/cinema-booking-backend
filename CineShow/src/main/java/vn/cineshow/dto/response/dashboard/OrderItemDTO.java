package vn.cineshow.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO cho thông tin order trong dashboard statistics
 */
@Data
@Builder
public class OrderItemDTO {
    private Long orderId;
    private String orderCode;
    private LocalDateTime createdAt;
    private Double totalPrice;
    private String orderStatus;

    // Thông tin người tạo order
    private Long userId;
    private String userName;
    private String userEmail;
}

