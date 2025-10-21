package vn.cineshow.repository;

import vn.cineshow.dto.request.movie.MovieFilterRequest;
import vn.cineshow.dto.request.movie.UserSearchMovieRequest;
import vn.cineshow.dto.response.PageResponse;

public interface SearchMovieRepositoryCustom {

    PageResponse<?> getMoviesListWithFilterByManyColumnAndSortBy(MovieFilterRequest request);

    PageResponse<?> findMoviesBySearchAndFilter(UserSearchMovieRequest request);
}
