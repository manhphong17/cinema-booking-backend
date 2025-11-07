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
public class MovieStatsResponse implements Serializable {
    private Long total;
    private Long playing;
    private Long upcoming;
    private Long ended;
}

