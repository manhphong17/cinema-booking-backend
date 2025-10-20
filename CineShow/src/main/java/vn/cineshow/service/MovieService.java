package vn.cineshow.service;

import vn.cineshow.dto.request.movie.*;
import vn.cineshow.dto.response.PageResponse;
import vn.cineshow.dto.response.movie.*;

import java.util.List;

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
}
