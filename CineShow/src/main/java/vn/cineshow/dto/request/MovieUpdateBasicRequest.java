package vn.cineshow.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class MovieUpdateBasicRequest {

    @NotNull
    long id;

    @NotNull(message = "Name cannot null")
    String name;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate releaseDate;

    @NotNull(message = "Genre list cannot be null")
    @Size(min = 1, message = "At least one genre is required")
    List<Long> genreIds;

    @NotNull
    Long languageId;

    @NotNull
    Long countryId;

    @NotNull
    String status;

}
