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
public class HotMovieResponse implements Serializable {
    private Long id;
    private String name;
    private String posterUrl;
    private Long bookingCount;
    private Long showtimeCount;
}

