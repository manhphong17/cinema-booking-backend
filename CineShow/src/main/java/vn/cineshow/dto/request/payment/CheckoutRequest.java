package vn.cineshow.dto.request.payment;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {

    private Long userId;
    private List<Long> ticketIds;
    private Long showtimeId;

    // Danh sách combo (concessionId + quantity)
    private List<ConcessionOrderRequest> concessions;

    private Double totalPrice;   // tổng tiền trước giảm giá
    private Double discount;     // số tiền giảm
    private Double amount;       // tổng tiền cuối cùng (sau giảm)
    private String paymentMethod; // "cash" | "vnpay"

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConcessionOrderRequest {
        private Long concessionId;
        private Integer quantity;
    }
}
