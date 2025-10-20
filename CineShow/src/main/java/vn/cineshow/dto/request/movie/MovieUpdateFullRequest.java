package vn.cineshow.dto.request.movie;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import vn.cineshow.utils.validator.FileType;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieUpdateFullRequest {

    @NotBlank(message = "Description must not blank")
    private String description;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

    @NotNull(message = "Release date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate releaseDate;

    @NotBlank(message = "Director must not blank")
    private String director;

    @NotBlank(message = "Actor must not blank")
    private String actor;

    @Min(0)
    private Integer ageRating;

    private String trailerUrl;

    @NotEmpty(message = "Genre list must not be empty")
    private List<Long> genreIds;

    @Min(1)
    private Long languageId;

    @Min(1)
    private Long countryId;

    @NotBlank
    private String status;

    @FileType(allowed = {"image/jpeg", "image/png"})
    private MultipartFile poster;

    @FileType(allowed = {"image/jpeg", "image/png"})
    private MultipartFile banner;
}

