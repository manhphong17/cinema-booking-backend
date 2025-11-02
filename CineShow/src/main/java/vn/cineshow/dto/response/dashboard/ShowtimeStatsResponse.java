package vn.cineshow.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeStatsResponse implements Serializable {
    private Long today;
    private Long thisWeek;
    private Long upcoming;
    private Long total;
}

