package vn.cineshow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import vn.cineshow.utils.validator.FileType;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class MovieCreationRequest {
    @NotNull(message = "Name cannot null")
    String name;

    @Length(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    String description;

    @Min(value = 1, message = "Duration must be greater than 0")
    Integer duration;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Future(message = "Release date must be in the future")
    LocalDate releaseDate;

    @NotNull
    String director;
    @NotNull
    String actor;

    Integer ageRating;
    String trailerUrl;

    @NotNull(message = "Genre list cannot be null")
    @Size(min = 1, message = "At least one genre is required")
    List<Long> genreIds;

    @NotNull(message = "Language cannot be null")
    Long languageId;

    @NotNull(message = "Country cannot be null")
    Long countryId;

    @NotNull(message = "File cannot null")
    @FileType(allowed = {"image/jpeg", "image/png"})
    @Schema(type = "string", format = "binary", description = "Poster image file")
    MultipartFile poster;
}
