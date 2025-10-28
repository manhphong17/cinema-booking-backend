package vn.cineshow.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import vn.cineshow.dto.request.movie.MovieFilterRequest;
import vn.cineshow.dto.request.movie.UserSearchMovieRequest;
import vn.cineshow.dto.response.PageResponse;
import vn.cineshow.dto.response.movie.CountryResponse;
import vn.cineshow.dto.response.movie.LanguageResponse;
import vn.cineshow.dto.response.movie.MovieGenreResponse;
import vn.cineshow.dto.response.movie.OperatorMovieOverviewResponse;
import vn.cineshow.dto.response.movie.UserMovieBookingListResponse;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.Movie;
import vn.cineshow.model.MovieGenre;

@Component
@Slf4j(topic = "SEARCH_REPOSITORY")
public class SearchMovieRepository implements SearchMovieRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PageResponse<?> getMoviesListWithFilterByManyColumnAndSortBy(MovieFilterRequest request) {

        //1. base query
        StringBuilder sqlQuery = baseQueryForGetMoviesListWithFilterByManyColumnAndSortBy(request, false);

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

        //2. Create select query
        Query selectQuery = entityManager.createQuery(sqlQuery.toString());
        selectQuery.setFirstResult(offset);
        selectQuery.setMaxResults(pageSize);
        setParameters(selectQuery, request);

        //3. execute query
        List<Movie> movies = selectQuery.getResultList();
        List<OperatorMovieOverviewResponse> responses = movies.stream().map(movie -> mapToMovieDetail(movie)).toList();

        //4. count
        StringBuilder sqlCountQuery = baseQueryForGetMoviesListWithFilterByManyColumnAndSortBy(request, true);

        Query selectCountQuery = entityManager.createQuery(sqlCountQuery.toString());
        setParameters(selectCountQuery, request);

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


    /**
     * Retrieves a paginated list of movies for end users.
     * Supports text search by name, filtering by genre and status,
     * automatically excludes deleted movies, and orders by release date & name descending.
     * <p>
     * Truy vấn danh sách phim cho người dùng, có tìm kiếm, lọc, sắp xếp, và phân trang.
     *
     * @param req user search and filter parameters
     * @return a PageResponse containing paginated movie results
     */
    @Override
    public PageResponse<?> findMoviesBySearchAndFilter(UserSearchMovieRequest req) {

        // === 1. Initialize the Criteria API ===
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Movie> cq = cb.createQuery(Movie.class);
        Root<Movie> movieRoot = cq.from(Movie.class);

        List<Predicate> predicates = new ArrayList<>();

        // === 2. Build dynamic filtering conditions ===

        //  1. Search by movie name
        if (StringUtils.hasText(req.getName())) {
            predicates.add(cb.like(movieRoot.get("name"), "%" + req.getName() + "%"));
        }

        // 2. Filter by genre (join Movie → MovieGenre)
        if (req.getGenreId() != null) {
            Join<Movie, MovieGenre> genreJoin = movieRoot.join("movieGenres", JoinType.INNER);
            predicates.add(cb.equal(genreJoin.get("id"), req.getGenreId()));
        }

        // 3. Filter by status (convert to uppercase for consistency)
        if (req.getStatus() != null && StringUtils.hasText(req.getStatus().name())) {
            predicates.add(cb.equal(movieRoot.get("status"), req.getStatus().name().toUpperCase()));
        }

        // 4. Exclude logically deleted movies
        predicates.add(cb.isFalse(movieRoot.get("isDeleted")));

        // === 3. Apply filters and default ordering ===
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(movieRoot.get("releaseDate")), cb.desc(movieRoot.get("name")));

        // === 4. Pagination setup ===
        int pageNo = Math.max(req.getPageNo(), 1);      // page index starts at 1
        int pageSize = Math.max(req.getPageSize(), 8); // default size if invalid
        int offset = (pageNo - 1) * pageSize;

        // === 5. Execute main data query ===
        TypedQuery<Movie> dataQuery = entityManager.createQuery(cq);
        dataQuery.setFirstResult(offset);
        dataQuery.setMaxResults(pageSize);
        List<Movie> movies = dataQuery.getResultList();

        // === 6. Count total results (for pagination info) ===
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Movie> countRoot = countQuery.from(Movie.class);
        List<Predicate> countPredicates = new ArrayList<>();

        // recreate predicates for count query
        if (StringUtils.hasText(req.getName())) {
            countPredicates.add(cb.like(countRoot.get("name"), "%" + req.getName() + "%"));
        }
        if (req.getGenreId() != null) {
            Join<Movie, MovieGenre> countGenreJoin = countRoot.join("movieGenres", JoinType.INNER);
            countPredicates.add(cb.equal(countGenreJoin.get("id"), req.getGenreId()));
        }
        if (req.getStatus() != null && StringUtils.hasText(req.getStatus().name())) {
            countPredicates.add(cb.equal(countRoot.get("status"), req.getStatus().name()));
        }
        countPredicates.add(cb.isFalse(countRoot.get("isDeleted")));

        countQuery.select(cb.countDistinct(countRoot))
                .where(countPredicates.toArray(new Predicate[0]));

        Long totalItems = entityManager.createQuery(countQuery).getSingleResult();

        List<UserMovieBookingListResponse> movieResponses = movies.stream()
                .map(movie -> mapToUserMovie(movie)).toList();

        // === 7. Build and return paginated response ===
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalItems(totalItems)
                .totalPages((int) Math.ceil((double) totalItems / pageSize))
                .items(movieResponses)
                .build();
    }


    private StringBuilder baseQueryForGetMoviesListWithFilterByManyColumnAndSortBy(MovieFilterRequest request, boolean count) {
        StringBuilder sql = new StringBuilder();
        if (count)
            sql.append("select count(distinct m) from Movie m left join m.movieGenres mg where m.isDeleted = false ");
        else
            sql.append("select distinct m from Movie m left join m.movieGenres mg where m.isDeleted = false ");

        if (StringUtils.hasLength(request.getKeyword()))
            sql.append(" and (lower(m.name) like lower(:name) or lower(m.country.name) like lower(:country))");

        if (StringUtils.hasLength(request.getGenre()))
            sql.append(" and lower(mg.name) like lower(:genre)");

        if (StringUtils.hasLength(request.getLanguage()))
            sql.append(" and lower(m.language.name) like lower(:language)");

        if (request.getFromDate() != null)
            sql.append(" and m.releaseDate >= :fromDate");

        if (request.getToDate() != null)
            sql.append(" and m.releaseDate <= :toDate");

        if (request.getStatuses() != null && !request.getStatuses().isEmpty() &&
                request.getStatuses().stream().anyMatch(StringUtils::hasText))
            sql.append(" and m.status in :statuses");

        return sql;
    }

    private void setParameters(Query query, MovieFilterRequest request) {
        if (StringUtils.hasText(request.getKeyword())) {
            query.setParameter("name", "%" + request.getKeyword() + "%");
            query.setParameter("country", "%" + request.getKeyword() + "%");
        }
        if (StringUtils.hasText(request.getGenre()))
            query.setParameter("genre", request.getGenre());
        if (StringUtils.hasText(request.getLanguage()))
            query.setParameter("language", request.getLanguage());
        if (request.getFromDate() != null)
            query.setParameter("fromDate", request.getFromDate());
        if (request.getToDate() != null)
            query.setParameter("toDate", request.getToDate());
        if (request.getStatuses() != null && !request.getStatuses().isEmpty() &&
                request.getStatuses().stream().anyMatch(StringUtils::hasText))
            query.setParameter("statuses", request.getStatuses().stream()
                    .filter(StringUtils::hasText).toList());
    }


    private OperatorMovieOverviewResponse mapToMovieDetail(Movie movie) {
        return OperatorMovieOverviewResponse.builder()
                .id(movie.getId())
                .name(movie.getName())
                .genre(movie.getMovieGenres().stream()
                        .map(g -> MovieGenreResponse.builder()
                                .id(g.getId())
                                .name(g.getName())
                                .build())
                        .toList())
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
                .isFeatured(movie.isFeatured())
                .build();
    }

    private UserMovieBookingListResponse mapToUserMovie(Movie movie) {
        return UserMovieBookingListResponse
                .builder()
                .posterUrl(movie.getPosterUrl())
                .name(movie.getName())
                .ageRating(movie.getAgeRating())
                .id(movie.getId())
                .build();
    }


}
