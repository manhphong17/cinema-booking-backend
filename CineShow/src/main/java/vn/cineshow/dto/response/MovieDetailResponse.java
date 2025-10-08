package vn.cineshow.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieDetailResponse implements Serializable {
    private Long id;
    private String actor;
    private String description;
    private String director;
    private String name;
    private String posterUrl;
    private LocalDate releaseDate;
    private String trailerUrl;
    private String status;
    private CountryResponse country;
    private LanguageResponse language;
    private List<MovieGenreResponse> genre;
    private int ageRating;
}
