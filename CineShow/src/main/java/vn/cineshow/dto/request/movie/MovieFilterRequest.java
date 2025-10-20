package vn.cineshow.dto.request.movie;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.utils.validator.ValidDateRange;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@ValidDateRange
public class MovieFilterRequest implements Serializable {
    String keyword;
    String genre;
    String director;
    String language;
    LocalDate fromDate;
    LocalDate toDate;
    List<String> statuses;
    String sortBy;
    @Min(value = 1, message = "Number of pages must be greater than or equal to 1")
    int pageNo = 1;
    @Min(value = 10, message = "Size of pages must be greater than or equal to 10")
    int pageSize = 10;
}
