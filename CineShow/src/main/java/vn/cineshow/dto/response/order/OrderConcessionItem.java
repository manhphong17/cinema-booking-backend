package vn.cineshow.dto.response.order;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderConcessionItem {
    String name;
    int quantity;
    Double unitPrice;
    String urlImage;
}
