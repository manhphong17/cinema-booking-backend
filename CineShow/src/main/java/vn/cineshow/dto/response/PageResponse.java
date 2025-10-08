package vn.cineshow.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse<T> {

    int pageNo;
    int pageSize;
    int totalPages;
    long totalItems;
    T items;
}
