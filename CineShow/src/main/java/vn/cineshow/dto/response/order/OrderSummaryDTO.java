package vn.cineshow.dto.response.order;
import vn.cineshow.enums.OrderStatus;
import java.time.LocalDateTime;

public record OrderSummaryDTO( Long id,
                               Long userId,
                               String userEmail,
                               Double totalPrice,
                               OrderStatus orderStatus,
                               LocalDateTime createdAt) {
}
