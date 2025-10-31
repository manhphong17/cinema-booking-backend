package vn.cineshow.dto.response.order;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class OrderListResponse {
    List<OrderListItemResponse> items;
    int page;
    int size;
    long totalElements;
    int totalPages;
}
