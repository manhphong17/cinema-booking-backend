package vn.cineshow.dto.request.movie;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import vn.cineshow.enums.MovieStatus;

@Getter
public class UserSearchMovieRequest {
    private String name;
    @Min(value = 1, message = "Genre id must be greater than or equal to 1")
    private Long genreId;

    @NotNull(message = "Movie status can not null")
    private MovieStatus status;

    @Min(value = 1, message = "Number of pages must be greater than or equal to 1")
    private int pageNo;

    @Min(value = 8, message = "Size of pages must be greater than or equal to 8")
    private int pageSize;
}
