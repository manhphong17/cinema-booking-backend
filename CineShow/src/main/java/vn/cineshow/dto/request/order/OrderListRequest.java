package vn.cineshow.dto.request.order;

import lombok.Data;

import java.util.List;

@Data
public class OrderListRequest {
    private Integer page;
    private Integer size;
    private List<String> sort;
}
