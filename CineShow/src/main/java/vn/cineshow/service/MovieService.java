package vn.cineshow.service;

import vn.cineshow.dto.request.MovieCreationRequest;
import vn.cineshow.dto.request.MovieFilterRequest;
import vn.cineshow.dto.request.MovieUpdateBasicRequest;
import vn.cineshow.dto.request.MovieUpdateFullRequest;
import vn.cineshow.dto.response.*;

import java.util.List;

public interface MovieService {
    PageResponse<?> getAllMovieWithSortBy(int pageNo, int pageSize, String sortBy);

    PageResponse<?> getMoviesWithFilterBymanyColumnAndSortBy(MovieFilterRequest filterRequest);

    List<LanguageResponse> getAllLanguage();

    List<MovieGenreResponse> getAllGenres();

    MovieDetailResponse getMovie(long id);

    Long create(MovieCreationRequest request);

    List<CountryResponse> getAllCountries();

    void updatebyId(long id, MovieUpdateBasicRequest request);

    void updateFullById(long id, MovieUpdateFullRequest request);

    void softDelete(long id);
}
