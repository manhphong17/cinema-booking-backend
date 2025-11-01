package vn.cineshow.service;

import java.time.LocalDate;
import java.util.List;

import vn.cineshow.dto.request.movie.MovieCreationRequest;
import vn.cineshow.dto.request.movie.MovieFilterRequest;
import vn.cineshow.dto.request.movie.MovieUpdateBasicRequest;
import vn.cineshow.dto.request.movie.MovieUpdateFullRequest;
import vn.cineshow.dto.request.movie.UserSearchMovieRequest;
import vn.cineshow.dto.response.PageResponse;
import vn.cineshow.dto.response.movie.BannerResponse;
import vn.cineshow.dto.response.movie.CountryResponse;
import vn.cineshow.dto.response.movie.LanguageResponse;
import vn.cineshow.dto.response.movie.MovieGenreResponse;
import vn.cineshow.dto.response.movie.OperatorMovieOverviewResponse;
import vn.cineshow.dto.response.movie.StaffMovieListResponse;

public interface MovieService {
    PageResponse<?> getAllMovieWithSortBy(int pageNo, int pageSize, String sortBy);

    PageResponse<?> getMoviesWithFilterBymanyColumnAndSortBy(MovieFilterRequest filterRequest);

    List<LanguageResponse> getAllLanguage();

    List<MovieGenreResponse> getAllGenres();

    OperatorMovieOverviewResponse getMovie(long id);

    Long create(MovieCreationRequest request);

    List<CountryResponse> getAllCountries();

    void updatebyId(long id, MovieUpdateBasicRequest request);

    void updateFullById(long id, MovieUpdateFullRequest request);

    void softDelete(long id);

    List<OperatorMovieOverviewResponse> getTopMovieForHomePage(String status, int limit);

    void updateFeatureMovie(long id, boolean isFeatured);

    List<BannerResponse> getBanners();

    PageResponse<?> getMovieListToBooking(UserSearchMovieRequest request);
    
    List<StaffMovieListResponse> getMoviesWithShowtimesOnDate(LocalDate date, String keyword);
}
