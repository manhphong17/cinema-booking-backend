package vn.cineshow.dto.request.order;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class OrderCreatedAtSearchRequest {
    private LocalDate date; // required: search by this day
    private Integer page;
    private Integer size;
    private List<String> sort; // ["asc"] or ["desc"], by createdAt
}
