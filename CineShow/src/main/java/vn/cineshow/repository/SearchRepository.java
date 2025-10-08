package vn.cineshow.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import vn.cineshow.dto.request.MovieFilterRequest;
import vn.cineshow.dto.response.*;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.Movie;
import vn.cineshow.model.MovieGenre;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j(topic = "SEARCH_REPOSITORY")
public class SearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public PageResponse<?> getMoviesListWithFilterByManyColumnAndSortBy(MovieFilterRequest request) {

        StringBuilder sqlQuery = new StringBuilder("select distinct  m from Movie m");
        //join movieGenres
        sqlQuery.append(" join m.movieGenres mg ");
        sqlQuery.append(" where m.isDeleted = false ");
        //search by movie name or country
        if (StringUtils.hasLength(request.getKeyword())) {
            sqlQuery.append(" and (lower(m.name) like lower(:name)");
            sqlQuery.append(" or lower(m.country.name) like lower(:country)) ");
        }

        //filter by a movie genres
        if (StringUtils.hasLength(request.getGenre())) {
            sqlQuery.append(" and lower(mg.name) like lower(:genre)");
        }

        //filter by a language
        if (StringUtils.hasLength(request.getLanguage())) {
            sqlQuery.append((" and lower(m.language.name) like lower(:language)"));
        }

        //filter by from date and to date

        if (request.getFromDate() != null) {
            sqlQuery.append(" and m.releaseDate >= :fromDate");
        }

        if (request.getToDate() != null) {
            sqlQuery.append(" and m.releaseDate <= :toDate");
        }

        //filter by list status
        if (request.getStatuses() != null && !request.getStatuses().isEmpty()
                && request.getStatuses().stream().anyMatch(StringUtils::hasText)) {
            sqlQuery.append(" and m.status in :statuses");
        }

        //order regex: column:desc/esc
        if (StringUtils.hasText(request.getSortBy())) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(asc|desc)");
            Matcher matcher = pattern.matcher(request.getSortBy());

            List<String> allowed = List.of("name", "releaseDate", "id");
            if (matcher.find() && (matcher.group(3).equalsIgnoreCase("asc") || matcher.group(3).equalsIgnoreCase("desc"))) {
                if (!allowed.contains(matcher.group(1))) {
                    throw new AppException(ErrorCode.INVALID_SORT_ORDER);
                }
                sqlQuery.append(String.format(" order by m.%s %s", matcher.group(1), matcher.group(3)));
            } else {
                throw new AppException(ErrorCode.INVALID_SORT_ORDER);
            }
        }

        int pageNo = request.getPageNo();
        int pageSize = request.getPageSize();
        if (pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize < 1) pageSize = 10;
        int offset = (pageNo - 1) * pageSize;

        Query selectQuery = entityManager.createQuery(sqlQuery.toString());
        selectQuery.setFirstResult(offset);
        selectQuery.setMaxResults(pageSize);
        if (StringUtils.hasText(request.getKeyword())) {
            selectQuery.setParameter("name", String.format("%%%s%%", request.getKeyword()));
            selectQuery.setParameter("country", String.format("%%%s%%", request.getKeyword()));
        }

        if (StringUtils.hasText(request.getGenre())) {
            selectQuery.setParameter("genre", request.getGenre());
        }

        if (StringUtils.hasText(request.getLanguage())) {
            selectQuery.setParameter("language", request.getLanguage());
        }

        if (request.getFromDate() != null) {
            selectQuery.setParameter("fromDate", request.getFromDate());
        }
        if (request.getToDate() != null) {
            selectQuery.setParameter("toDate", request.getToDate());
        }

        if (request.getStatuses() != null && !request.getStatuses().isEmpty()
                && request.getStatuses().stream().anyMatch(StringUtils::hasText)) {
            selectQuery.setParameter("statuses",
                    request.getStatuses().stream().filter(StringUtils::hasText).toList());
        }


        List<Movie> movies = selectQuery.getResultList();


        /*List<MovieDetailResponse> responses = movies.stream().map(movie -> MovieDetailResponse.builder()
                .id(movie.getId())
                .actor(movie.getActor())
                .name(movie.getName())
                .genre(getMovieGenresByMovie(movie))
                .country(CountryResponse.builder()
                        .id(movie.getCountry().getId())
                        .name(movie.getCountry().getName())
                        .build())
                .description(movie.getDescription())
                .releaseDate(movie.getReleaseDate())
                .trailerUrl(movie.getTrailerUrl())
                .language(LanguageResponse.builder()
                        .id(movie.getLanguage().getId())
                        .name(movie.getLanguage().getName())
                        .build())
                .posterUrl(movie.getPosterUrl())
                .director(movie.getDirector())
                .status(movie.getStatus().name())
                .ageRating(movie.getAgeRating())
                .build()).toList();*/

        List<MovieDetailResponse> responses = movies.stream().map(movie -> MovieDetailResponse.builder()
                .id(movie.getId())
                .name(movie.getName())
                .genre(getMovieGenresByMovie(movie))
                .country(CountryResponse.builder()
                        .id(movie.getCountry().getId())
                        .name(movie.getCountry().getName())
                        .build())
                .releaseDate(movie.getReleaseDate())
                .language(LanguageResponse.builder()
                        .id(movie.getLanguage().getId())
                        .name(movie.getLanguage().getName())
                        .build())
                .status(movie.getStatus().name())
                .build()).toList();

        //count query
        StringBuilder sqlCountQuery = new StringBuilder("select count(distinct m) from Movie m ");
        sqlCountQuery.append(" join m.movieGenres mg ");
        sqlCountQuery.append(" where m.isDeleted = false ");
        //search by movie name or country
        if (StringUtils.hasLength(request.getKeyword())) {
            sqlCountQuery.append(" and (lower(m.name) like lower(:name)");
            sqlCountQuery.append(" or lower(m.country.name) like lower(:country))");
        }

        //filter by a movie genres
        if (StringUtils.hasLength(request.getGenre())) {
            sqlCountQuery.append(" and lower(mg.name) like lower(:genre)");
        }

        //filter by a language
        if (StringUtils.hasLength(request.getLanguage())) {
            sqlCountQuery.append((" and lower(m.language.name) like lower(:language)"));
        }

        //filter by from date and to date

        if (request.getFromDate() != null) {
            sqlCountQuery.append(" and m.releaseDate >= :fromDate");
        }

        if (request.getToDate() != null) {
            sqlCountQuery.append(" and m.releaseDate <= :toDate");
        }

        //filter by list status

        if (request.getStatuses() != null) {
            sqlCountQuery.append(" and m.status in :statuses");
        }

        Query selectCountQuery = entityManager.createQuery(sqlCountQuery.toString());
        if (StringUtils.hasText(request.getKeyword())) {
            selectCountQuery.setParameter("name", String.format("%%%s%%", request.getKeyword()));
            selectCountQuery.setParameter("country", String.format("%%%s%%", request.getKeyword()));
        }

        if (StringUtils.hasText(request.getGenre())) {
            selectCountQuery.setParameter("genre", request.getGenre());
        }

        if (StringUtils.hasText(request.getLanguage())) {
            selectCountQuery.setParameter("language", request.getLanguage());
        }

        if (request.getFromDate() != null) {
            selectCountQuery.setParameter("fromDate", request.getFromDate());
        }
        if (request.getToDate() != null) {
            selectCountQuery.setParameter("toDate", request.getToDate());
        }

        if (request.getStatuses() != null) {
            selectCountQuery.setParameter("statuses", request.getStatuses());
        }

        long total = (long) selectCountQuery.getSingleResult();
        log.info("total of movies returned: " + total);

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .items(responses)
                .totalPages((int) Math.ceil((double) total / pageSize))
                .totalItems(total)
                .build();
    }

    private List<MovieGenreResponse> getMovieGenresByMovie(Movie movie) {
        List<MovieGenre> movieGenres = movie.getMovieGenres().stream().toList();
        return movieGenres.stream().map(movieGenre -> MovieGenreResponse.builder()
                        .id(movieGenre.getId())
                        .name(movieGenre.getName())
                        .build())
                .toList();
    }
}
