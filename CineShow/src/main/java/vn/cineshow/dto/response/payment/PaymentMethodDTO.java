package vn.cineshow.dto.response.payment;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodDTO {
    private String paymentName;
    private String paymentCode;
    private String imageUrl;
}
