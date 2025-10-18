package vn.cineshow.dto.response.showtime;

import lombok.*;

import java.time.LocalDateTime;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ShowTimeResponse {
    private Long id;
    private Long movieId;
    private Long roomId;
    private Long subtitleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
