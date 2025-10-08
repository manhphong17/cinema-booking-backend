package vn.cineshow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import vn.cineshow.utils.validator.FileType;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class MovieUpdateFullRequest {
    @NotNull
    long id;

    @NotNull(message = "Name cannot null")
    String name;

    String description;

    Integer duration;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate releaseDate;

    String director;
    String actor;

    Integer ageRating;
    String trailerUrl;

    @NotNull(message = "Genre list cannot be null")
    @Size(min = 1, message = "At least one genre is required")
    List<Long> genreIds;

    Long languageId;

    Long countryId;

    String status;

    @FileType(allowed = {"image/jpeg", "image/png"})
    @Schema(type = "string", format = "binary", description = "Poster image file")
    MultipartFile poster;
}
