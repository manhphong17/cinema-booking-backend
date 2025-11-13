// trong /dto/response/DailyStatDTO.java
package vn.cineshow.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyStatDTO {
    private LocalDate date;
    private long count;
}