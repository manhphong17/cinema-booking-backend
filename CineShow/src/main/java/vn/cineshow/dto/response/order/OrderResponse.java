package vn.cineshow.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
        private Long id;
        private String code;
        private String customerName;
        private LocalDateTime createdAt;
        private Double totalPrice;
        private String status;
        private String paymentMethod;

}
