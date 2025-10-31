package vn.cineshow.dto.request.order;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDateSearchRequest {
    private LocalDateTime start;
    private LocalDateTime end;
    private Integer page;
    private Integer size;
    private List<String> sort; // ["asc"] or ["desc"]
}
