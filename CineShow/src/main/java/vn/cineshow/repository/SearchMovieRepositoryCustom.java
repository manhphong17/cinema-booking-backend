package vn.cineshow.repository;

import java.time.LocalDate;
import java.util.List;

import vn.cineshow.dto.request.movie.MovieFilterRequest;
import vn.cineshow.dto.request.movie.UserSearchMovieRequest;
import vn.cineshow.dto.response.PageResponse;
import vn.cineshow.dto.response.movie.StaffMovieListResponse;

public interface SearchMovieRepositoryCustom {

    PageResponse<?> getMoviesListWithFilterByManyColumnAndSortBy(MovieFilterRequest request);

    PageResponse<?> findMoviesBySearchAndFilter(UserSearchMovieRequest request);
    
    List<StaffMovieListResponse> findMoviesWithShowtimesOnDate(LocalDate date, String keyword);
}
