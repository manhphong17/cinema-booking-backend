package vn.cineshow.dto.response.movie;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDetailResponse {
    private Long id;
    private String name;
    private String posterUrl;
    private Integer duration;
    private LocalDate releaseDate;
    private String country;
    private List<String> genres;
    private int ageRating;
}
