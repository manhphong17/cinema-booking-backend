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
public class PlayingMovieResponse implements Serializable {
    private Long id;
    private String name;
    private String posterUrl;
    private Long showtimeCount;
    private String status; // "PLAYING", "UPCOMING", "ENDED"
}

