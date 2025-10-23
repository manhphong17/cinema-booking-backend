package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.cineshow.dto.request.movie.*;
import vn.cineshow.dto.response.PageResponse;
import vn.cineshow.dto.response.movie.*;
import vn.cineshow.enums.MovieStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.DuplicateResourceException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.Country;
import vn.cineshow.model.Language;
import vn.cineshow.model.Movie;
import vn.cineshow.model.MovieGenre;
import vn.cineshow.repository.*;
import vn.cineshow.service.MovieService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "Movie_Service")
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final SearchMovieRepository searchRepository;
    private final LanguageRepository languageRepository;
    private final MovieGenresRepository movieGenresRepository;
    private final CountryRepository countryRepository;

    private final S3Service s3Service;

    @Override
    public PageResponse<?> getAllMovieWithSortBy(int pageNo, int pageSize, String sortBy) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }

        List<Sort.Order> sorts = new ArrayList<>();

        //if sortBy != null
        if (StringUtils.hasLength(sortBy)) {
            //firstName:asc|desc
            Pattern pattern = Pattern.compile("(\\w+)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                } else if (matcher.group(3).equalsIgnoreCase("desc")) {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                } else {
                    throw new IllegalArgumentException("Invalid sort parameter");
                }
            }
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));
        Page<Movie> movies = movieRepository.findAll(pageable);
        log.info("Movies found: totalElements={}, totalPages={}, size={}",
                movies.getTotalElements(), movies.getTotalPages(), movies.getSize());

        return getPageResponse(pageNo, pageSize, movies);
    }

    @Override
    public PageResponse<?> getMoviesWithFilterBymanyColumnAndSortBy(MovieFilterRequest filterRequest) {
        return searchRepository.getMoviesListWithFilterByManyColumnAndSortBy(filterRequest);
    }


    @Override
    public List<LanguageResponse> getAllLanguage() {
        return languageRepository.findAll()
                .stream().map(language -> LanguageResponse.builder()
                        .id(language.getId())
                        .name(language.getName())
                        .build())
                .toList();
    }

    @Override
    public List<MovieGenreResponse> getAllGenres() {
        return movieGenresRepository.findAll()
                .stream()
                .map(movieGenre -> MovieGenreResponse.builder()
                        .id(movieGenre.getId())
                        .name(movieGenre.getName())
                        .build())
                .toList();
    }

    @Override
    public OperatorMovieOverviewResponse getMovie(long id) {
        Movie movie = findById(id);
        log.info("Movie found, id: {}", id);
        return OperatorMovieOverviewResponse.builder()
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
                .bannerUrl(movie.getBannerUrl())
                .language(LanguageResponse.builder()
                        .id(movie.getLanguage().getId())
                        .name(movie.getLanguage().getName())
                        .build())
                .posterUrl(movie.getPosterUrl())
                .director(movie.getDirector())
                .ageRating(movie.getAgeRating())
                .posterUrl(movie.getPosterUrl())
                .duration(movie.getDuration())
                .status(movie.getStatus().name())
                .build();
    }

    @Override
    @Transactional
    public Long create(MovieCreationRequest request) {
        //check language exist
        Language language = findLanguageById(request.getLanguageId());

        // get Country
        Country country = findCountryById(request.getCountryId());

        //get Movie Genres
        Set<MovieGenre> movieGenres = new HashSet<>();
        for (Long genreId : request.getGenreIds()) {
            movieGenres.add(findMovieGenreById(genreId));
        }

        if (isMovieExist(request.getName(), request.getReleaseDate())) {
            log.warn("Movie with name {} already exists", request.getName());
            throw new DuplicateResourceException("Movie already exists");
        }

        try {
            String posterUrl = s3Service.uploadFile(request.getPoster());

            log.info("Creation movie: name:{}, poster: {}", request.getPoster(), posterUrl);
            Movie movie = Movie.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .duration(request.getDuration())
                    .releaseDate(request.getReleaseDate())
                    .director(request.getDirector())
                    .actor(request.getActor())
                    .ageRating(request.getAgeRating())
                    .trailerUrl(request.getTrailerUrl())
                    .movieGenres(movieGenres)
                    .country(country)
                    .language(language)
                    .posterUrl(posterUrl)
                    .status(MovieStatus.UPCOMING)
                    .build();
            movieRepository.save(movie);
            log.info("Movie created successfully, id: {}", movie.getId());
            return movie.getId();
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public List<CountryResponse> getAllCountries() {
        return countryRepository.findAll().stream().map(country -> CountryResponse.builder()
                .name(country.getName())
                .id(country.getId())
                .build()).toList();
    }

    @Transactional
    @Override
    public void updatebyId(long id, MovieUpdateBasicRequest request) {
        Movie movie = findById(id);

        Set<MovieGenre> movieGenres = new HashSet<>();
        for (Long genreId : request.getGenreIds()) {
            movieGenres.add(findMovieGenreById(genreId));
        }

        movie.setName(request.getName());
        movie.setLanguage(findLanguageById(request.getLanguageId()));
        movie.setCountry(findCountryById(request.getCountryId()));
        movie.setMovieGenres(movieGenres);
        movie.setStatus(MovieStatus.valueOf(request.getStatus()));
        movie.setReleaseDate(request.getReleaseDate());

        movieRepository.save(movie);
        log.info("Movie updated successfully, id: {}", movie.getId());
    }

    @Transactional
    @Override
    public void updateFullById(long id, MovieUpdateFullRequest request) {
        Movie movie = findById(id);

        Set<MovieGenre> movieGenres = new HashSet<>();
        for (Long genreId : request.getGenreIds()) {
            movieGenres.add(findMovieGenreById(genreId));
        }
        try {

            if (request.getPoster() != null && !request.getPoster().isEmpty()) {
                String posterUrl = s3Service.uploadFile(request.getPoster());
                movie.setPosterUrl(posterUrl);
            }

            // Banner
            if (request.getBanner() != null && !request.getBanner().isEmpty()) {
                String bannerUrl = s3Service.uploadFile(request.getBanner());
                movie.setBannerUrl(bannerUrl);
            }

            movie.setReleaseDate(request.getReleaseDate());
            if (request.getDuration() != null) {
                movie.setDuration(request.getDuration());
            }
            if (request.getAgeRating() != null) {
                movie.setAgeRating(request.getAgeRating());
            }
            movie.setActor(request.getActor());
            movie.setDirector(request.getDirector());
            movie.setCountry(findCountryById(request.getCountryId()));
            movie.setLanguage(findLanguageById(request.getLanguageId()));
            movie.setMovieGenres(movieGenres);
            movie.setTrailerUrl(request.getTrailerUrl());
            movie.setDescription(request.getDescription());
            movie.setStatus(MovieStatus.valueOf(request.getStatus()));

            movieRepository.save(movie);
            log.info("Movie updated successfully, id: {}", movie.getId());
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Transactional
    @Override
    public void softDelete(long id) {
        Movie movie = findById(id);
        movie.setDeleted(true);
        movie.setStatus(MovieStatus.ENDED);
        movieRepository.save(movie);
        log.info("Movie deleted successfully, id: {}", movie.getId());
    }

    @Override
    public List<OperatorMovieOverviewResponse> getTopMovieForHomePage(String movieStatus, int limit) {
        try {
            MovieStatus status = MovieStatus.valueOf(movieStatus.toUpperCase());
            List<Movie> topMovies = movieRepository.findTopNMovieByStatus(status, PageRequest.of(0, limit));
            return topMovies.stream()
                    .map(movie -> OperatorMovieOverviewResponse
                            .builder()
                            .id(movie.getId())
                            .name(movie.getName())
                            .ageRating(movie.getAgeRating())
                            .posterUrl(movie.getPosterUrl())
                            .status(movie.getStatus().name())
                            .build())
                    .toList();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Movie status invalid");
        }
    }

    @Override
    public void updateFeatureMovie(long id, boolean isFeatured) {
        Movie movie = findById(id);
        movie.setFeatured(isFeatured);
        movieRepository.save(movie);
        log.info("Movie updated successfully, id: {}", movie.getId());
    }

    @Override
    public List<BannerResponse> getBanners() {

        List<Movie> movies = movieRepository.findAllFeaturedMovies();

        return movies.stream()
                .map(movie -> BannerResponse
                        .builder()
                        .movieId(movie.getId())
                        .bannerUrl(movie.getBannerUrl())
                        .build())
                .toList();
    }

    @Override
    public PageResponse<?> getMovieListToBooking(UserSearchMovieRequest request) {
        return searchRepository.findMoviesBySearchAndFilter(request);
    }


    private PageResponse<?> getPageResponse(int pageNo, int pageSize, Page<Movie> movies) {

        List<OperatorMovieOverviewResponse> responses = movies.stream().map(movie -> OperatorMovieOverviewResponse.builder()
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
                .build()).toList();

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(movies.getTotalPages())
                .items(responses)
                .build();

    }

    private boolean isMovieExist(String name, LocalDate releaseDate) {
        return movieRepository.existsByNameAndReleaseDate(name, releaseDate);
    }

    private List<MovieGenreResponse> getMovieGenresByMovie(Movie movie) {
        List<MovieGenre> movieGenres = movie.getMovieGenres().stream().toList();
        return movieGenres.stream().map(movieGenre -> MovieGenreResponse.builder()
                        .id(movieGenre.getId())
                        .name(movieGenre.getName())
                        .build())
                .toList();
    }

    private Movie findById(Long id) {
        log.info("Finding movie by id: {}", id);
        return movieRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
    }

    private Language findLanguageById(Long id) {
        log.info("Finding language by id: {}", id);
        return languageRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.LANGUAGE_NOT_FOUND));
    }

    private Country findCountryById(Long id) {
        log.info("Finding country by id: {}", id);
        return countryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.COUNTRY_NOT_FOUND));
    }

    private MovieGenre findMovieGenreById(Long id) {
        log.info("Finding movie genre by id: {}", id);
        return movieGenresRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.MOVIE_GENRE_NOT_FOUND));
    }
}
