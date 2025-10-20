package vn.cineshow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.movie.*;
import vn.cineshow.dto.response.PageResponse;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.movie.*;
import vn.cineshow.service.MovieService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/movies")

@RequiredArgsConstructor
@Tag(name = "Movie Controller")
@Slf4j(topic = "MOVIE-CONTROLLER")
public class MovieController {

    private final MovieService movieService;

    @Operation(summary = "Get all movies with sort by",
            description = "Send a request via this API to get all movies with sort by")
    @GetMapping("/list-with-sortBy")
    public ResponseData<PageResponse<?>> getMoviesList(@Min(1) @RequestParam(defaultValue = "1", required = false) int pageNo,
                                                       @Min(value = 10) @RequestParam(defaultValue = "10", required = false) int pageSize,
                                                       @RequestParam(required = false) String sortBy) {
        log.info("Request getMoviesList, pageNo: {}, pageSize: {}", pageNo, pageSize);
        PageResponse<?> movies = movieService.getAllMovieWithSortBy(pageNo, pageSize, sortBy);
        log.info("Response getMoviesList, pageNo: {}, pageSize: {}", pageNo, pageSize);
        return new ResponseData<>(HttpStatus.OK.value(), "Movies founded successfully", movies);
    }

    @Operation(
            summary = "Get movies list with filter by many columns and sort by",
            description = "Send a request via this API to get movies list with filter by many columns and sort by"
    )
    @GetMapping("/list-with-filter-many-column-and-sortBy")
    public ResponseData<?> getMoviesListWithFilterByManyColumnAndSortBy(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String director,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) String sortBy,
            @Min(1) @RequestParam(defaultValue = "1") int pageNo,
            @Min(value = 8) @RequestParam(defaultValue = "10") int pageSize
    ) {
        log.info("Request get movies list with filter, pageNo: {}, pageSize: {}", pageNo, pageSize);

        String defaultSortBy = (sortBy == null || sortBy.isEmpty()) ? "id:asc" : sortBy;

        MovieFilterRequest filterRequest = MovieFilterRequest.builder()
                .keyword(keyword)
                .genre(genre)
                .director(director)
                .language(language)
                .fromDate(fromDate)
                .toDate(toDate)
                .statuses(statuses)
                .sortBy(defaultSortBy)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .build();

        PageResponse<?> movies = movieService.getMoviesWithFilterBymanyColumnAndSortBy(filterRequest);

        log.info("Response get movies list with filter , pageNo: {}, pageSize: {}", pageNo, pageSize);

        return new ResponseData<>(HttpStatus.OK.value(), "Movies founded successfully", movies);
    }

    @Operation(
            summary = "Get all languages",
            description = "Send a request via this API to get all languages"
    )
    @GetMapping("/languages")
    public ResponseData<List<LanguageResponse>> getAllLanguage() {
        log.info("Request getAllLanguages");

        List<LanguageResponse> languages = movieService.getAllLanguage();

        return new ResponseData<>(HttpStatus.OK.value(), "Languages founded successfully", languages);
    }

    @Operation(
            summary = "Get all Movie Genres",
            description = "Send a request via this API to get all movie menres"
    )
    @GetMapping("/movie-genres")
    public ResponseData<List<MovieGenreResponse>> getAllMovieGenre() {
        log.info("Request get all movie genres");

        List<MovieGenreResponse> genres = movieService.getAllGenres();

        return new ResponseData<>(HttpStatus.OK.value(), "Movie genres founded successfully", genres);
    }

    @Operation(
            summary = "Get all countries",
            description = "Send a request via this API to get all countries"
    )
    @GetMapping("/countries")
    public ResponseData<List<CountryResponse>> getAllCountries() {
        log.info("Request get all country");

        List<CountryResponse> countries = movieService.getAllCountries();

        log.info("Get all countries successfully");
        return new ResponseData<>(HttpStatus.OK.value(), "Get all countries successfully", countries);
    }

    @Operation(
            summary = "Get a movies by id",
            description = "Send a request via this API to get movie by id"
    )
    @GetMapping("/{id}")
    public ResponseData<OperatorMovieOverviewResponse> getMovieById(@PathVariable long id) {
        log.info("Request get movie by id: {}", id);

        OperatorMovieOverviewResponse movie = movieService.getMovie(id);

        log.info("Response get movie by id: {}", id);
        return new ResponseData<>(HttpStatus.OK.value(), "Get movie by id successfully", movie);
    }

    @Operation(
            summary = "Create a movie",
            description = "Send a request via this API to create a new movie"
    )
    @PostMapping(value = "/add", consumes = "multipart/form-data")
    public ResponseData<Long> createMovie(@Valid @ModelAttribute MovieCreationRequest request) {
        log.info("Request create movie: {}", request);

        Long movieId = movieService.create(request);

        log.info("Response create movie, id:{}", movieId);
        return new ResponseData<>(HttpStatus.OK.value(), "Movie created successfully", movieId);
    }

    @Operation(
            summary = "Update a movie by id",
            description = "Send a request via this API to update a movie by id"
    )
    @PutMapping(value = "/update/{id}")
    public ResponseData<?> updateSomeFailedById(@PathVariable long id, @Valid @RequestBody MovieUpdateBasicRequest request) {

        log.info("Request update movie: {}", request);
        movieService.updatebyId(id, request);
        log.info("Response update movie: {}", request);
        return new ResponseData<>(HttpStatus.OK.value(), "Movie updated successfully");
    }

    @Operation(
            summary = "Update complete movie information by id",
            description = "Send a request via this API to update complete movie information by id including poster and banner files"
    )
    @PutMapping(value = "/update-full/{movieId}", consumes = {"multipart/form-data"})
    public ResponseData<?> updateFullMovieById(
            @PathVariable @Min(1) long movieId,
            @Valid @ModelAttribute MovieUpdateFullRequest request
    ) {
        log.info("Request update full movie: id={}", movieId);
        movieService.updateFullById(movieId, request);
        log.info("Response update full movie: id={}", movieId);
        return new ResponseData<>(HttpStatus.OK.value(), "Movie updated successfully");
    }

    @Operation(
            summary = "Soft delete movie",
            description = "Send a request via this API to soft delete movie"
    )
    @PatchMapping("/delete/{id}")
    public ResponseData<?> softDeleteMovie(@PathVariable long id) {
        log.info("Request delete movie: {}", id);
        movieService.softDelete(id);
        log.info("Response delete movie: {}", id);
        return new ResponseData<>(HttpStatus.OK.value(), "Movie deleted successfully");
    }

    @Operation(
            summary = "Update feature of movie",
            description = "Send a request via this API to update feature of movie"
    )
    @PatchMapping("/update-feature/{id}")
    public ResponseData<?> updateFeatureMovie(@PathVariable long id, @RequestBody Map<String, Boolean> request) {
        boolean isFeatured = request.get("isFeatured");
        movieService.updateFeatureMovie(id, isFeatured);
        return new ResponseData<>(HttpStatus.OK.value(), "Movie updated successfully");
    }


    @Operation(
            summary = "Get top movie (now showing/ up coming) to show in homepage",
            description = "Send a request via this API to get top movie to show in homepage"
    )
    @GetMapping("/top/{limit}")
    public ResponseData<List<OperatorMovieOverviewResponse>> getTopMoviesForHomePage(@RequestParam @NotNull String movieStatus, @Min(4) @PathVariable int limit) {

        log.info("Request to get top movies in home page");
        List<OperatorMovieOverviewResponse> movies = movieService.getTopMovieForHomePage(movieStatus, limit);
        log.info("Response get top movie successfully");
        return new ResponseData<>(HttpStatus.OK.value(), "Top movies in home page", movies);

    }

    @Operation(
            summary = "Get movie banners to show in homepage",
            description = "Send a request via this API to get movie banners to show in homepage"
    )
    @GetMapping("/banners")
    public ResponseData<List<BannerResponse>> getBanners() {

        log.info("Request to get movie banner into homepage");
        List<BannerResponse> bannerResponses = movieService.getBanners();
        log.info("Response to get movie banner into homepage");
        return new ResponseData<>(HttpStatus.OK.value(), "get movie banner into homepage successfully", bannerResponses);
    }

    @Operation(
            summary = "Used by end users to browse and select movies for booking",
            description = "Get movie banners to display on homepage for users before booking."
    )
    @PostMapping("/search")
    public ResponseData<?> getMovieListToBooking(@RequestBody UserSearchMovieRequest request) {

        log.info("Request to get movie list to booking");
        PageResponse<?> movieList = movieService.getMovieListToBooking(request);
        log.info("Response to get movie list to booking");

        return new ResponseData<>(HttpStatus.OK.value(), "Get movie list to booking successfully", movieList);
    }
}
