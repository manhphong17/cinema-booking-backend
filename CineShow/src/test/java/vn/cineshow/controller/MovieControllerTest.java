package vn.cineshow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
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
import vn.cineshow.service.MovieService;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.impl.AccountDetailsService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.stubbing.Answer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MovieController.class)
@AutoConfigureMockMvc(addFilters = false)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieService movieService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AccountDetailsService accountDetailsService;

    private OperatorMovieOverviewResponse mockMovie;

    @BeforeEach
    void setUp() {
        // Setup mock movie response
        mockMovie = OperatorMovieOverviewResponse.builder()
                .id(1L)
                .name("Test Movie")
                .build();
    }

    @Test
    @DisplayName("GET /movies/languages should return all languages")
    void getAllLanguages_shouldReturnLanguagesList() throws Exception {
        List<LanguageResponse> languages = Arrays.asList(
                LanguageResponse.builder().id(1L).name("English").build(),
                LanguageResponse.builder().id(2L).name("Vietnamese").build()
        );

        when(movieService.getAllLanguage()).thenReturn(languages);

        mockMvc.perform(get("/movies/languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Languages founded successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("English"))
                .andExpect(jsonPath("$.data[1].name").value("Vietnamese"));

        verify(movieService, times(1)).getAllLanguage();
    }

    @Test
    @DisplayName("GET /movies/movie-genres should return all movie genres")
    void getAllMovieGenres_shouldReturnGenresList() throws Exception {
        List<MovieGenreResponse> genres = Arrays.asList(
                MovieGenreResponse.builder().id(1L).name("Action").build(),
                MovieGenreResponse.builder().id(2L).name("Comedy").build()
        );

        when(movieService.getAllGenres()).thenReturn(genres);

        mockMvc.perform(get("/movies/movie-genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Movie genres founded successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Action"));

        verify(movieService, times(1)).getAllGenres();
    }

    @Test
    @DisplayName("GET /movies/countries should return all countries")
    void getAllCountries_shouldReturnCountriesList() throws Exception {
        List<CountryResponse> countries = Arrays.asList(
                CountryResponse.builder().id(1L).name("United States").build(),
                CountryResponse.builder().id(2L).name("Vietnam").build()
        );

        when(movieService.getAllCountries()).thenReturn(countries);

        mockMvc.perform(get("/movies/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get all countries successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("United States"));

        verify(movieService, times(1)).getAllCountries();
    }

    @Test
    @DisplayName("GET /movies/{id} should return movie by id")
    void getMovieById_shouldReturnMovie() throws Exception {
        when(movieService.getMovie(1L)).thenReturn(mockMovie);

        mockMvc.perform(get("/movies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get movie by id successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Test Movie"));

        verify(movieService, times(1)).getMovie(1L);
    }

    @Test
    @DisplayName("PUT /movies/update/{id} should update movie")
    void updateMovie_shouldUpdateSuccessfully() throws Exception {
        MovieUpdateBasicRequest request = MovieUpdateBasicRequest.builder()
                .name("Updated Movie Title")
                .status("NOW_SHOWING")
                .languageId(1L)
                .countryId(1L)
                .genreIds(Arrays.asList(1L, 2L))
                .releaseDate(LocalDate.now())
                .build();

        doNothing().when(movieService).updatebyId(eq(1L), any(MovieUpdateBasicRequest.class));

        mockMvc.perform(put("/movies/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Movie updated successfully"));

        ArgumentCaptor<MovieUpdateBasicRequest> captor = ArgumentCaptor.forClass(MovieUpdateBasicRequest.class);
        verify(movieService).updatebyId(eq(1L), captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Updated Movie Title");
    }

    @Test
    @DisplayName("PATCH /movies/delete/{id} should soft delete movie")
    void softDeleteMovie_shouldDeleteSuccessfully() throws Exception {
        doNothing().when(movieService).softDelete(1L);

        mockMvc.perform(patch("/movies/delete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Movie deleted successfully"));

        verify(movieService, times(1)).softDelete(1L);
    }

    @Test
    @DisplayName("PATCH /movies/update-feature/{id} should update movie feature status")
    void updateFeatureMovie_shouldUpdateSuccessfully() throws Exception {
        Map<String, Boolean> request = new HashMap<>();
        request.put("isFeatured", true);

        doNothing().when(movieService).updateFeatureMovie(1L, true);

        mockMvc.perform(patch("/movies/update-feature/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Movie updated successfully"));

        verify(movieService, times(1)).updateFeatureMovie(1L, true);
    }

    @Test
    @DisplayName("GET /movies/top/{limit} should return top movies")
    void getTopMovies_shouldReturnMoviesList() throws Exception {
        List<OperatorMovieOverviewResponse> topMovies = Arrays.asList(mockMovie);

        when(movieService.getTopMovieForHomePage("NOW_SHOWING", 5)).thenReturn(topMovies);

        mockMvc.perform(get("/movies/top/5")
                        .param("movieStatus", "NOW_SHOWING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Top movies in home page"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L));

        verify(movieService, times(1)).getTopMovieForHomePage("NOW_SHOWING", 5);
    }

    @Test
    @DisplayName("GET /movies/banners should return movie banners")
    void getBanners_shouldReturnBannersList() throws Exception {
        List<BannerResponse> banners = Arrays.asList(
                BannerResponse.builder()
                        .movieId(1L)
                        .bannerUrl("http://example.com/banner.jpg")
                        .build()
        );

        when(movieService.getBanners()).thenReturn(banners);

        mockMvc.perform(get("/movies/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("get movie banner into homepage successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].movieId").value(1L))
                .andExpect(jsonPath("$.data[0].bannerUrl").value("http://example.com/banner.jpg"));

        verify(movieService, times(1)).getBanners();
    }

    @Test
    @DisplayName("GET /movies/list-with-filter-many-column-and-sortBy should return filtered movies with sortBy")
    void getMoviesWithFilter_shouldReturnFilteredMovies() throws Exception {
        // Test branch 2: sortBy != null && !isEmpty() → use sortBy value
        when(movieService.getMoviesWithFilterBymanyColumnAndSortBy(any(MovieFilterRequest.class)))
                .thenAnswer((Answer<PageResponse<?>>) invocation -> {
                    PageResponse<Object> response = PageResponse.<Object>builder()
                            .pageNo(1)
                            .pageSize(10)
                            .totalPages(1)
                            .totalItems(0L)
                            .items(null)
                            .build();
                    return (PageResponse<?>) response;
                });

        mockMvc.perform(get("/movies/list-with-filter-many-column-and-sortBy")
                        .param("keyword", "spider")
                        .param("genre", "Action")
                        .param("sortBy", "name:asc")  // Branch 2: sortBy != null && !isEmpty()
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());

        ArgumentCaptor<MovieFilterRequest> captor = ArgumentCaptor.forClass(MovieFilterRequest.class);
        verify(movieService, times(1)).getMoviesWithFilterBymanyColumnAndSortBy(captor.capture());
        
        // Verify that provided sortBy "name:asc" is used when sortBy is not null and not empty
        assertThat(captor.getValue().getSortBy()).isEqualTo("name:asc");
    }

    @Test
    @DisplayName("GET /movies/list-with-filter-many-column-and-sortBy with null sortBy should use default sortBy")
    void getMoviesWithFilter_nullSortBy_shouldUseDefaultSortBy() throws Exception {
        // Test branch 1: sortBy == null → use "id:desc"
        when(movieService.getMoviesWithFilterBymanyColumnAndSortBy(any(MovieFilterRequest.class)))
                .thenAnswer((Answer<PageResponse<?>>) invocation -> {
                    PageResponse<Object> response = PageResponse.<Object>builder()
                            .pageNo(1)
                            .pageSize(10)
                            .totalPages(1)
                            .totalItems(0L)
                            .items(null)
                            .build();
                    return (PageResponse<?>) response;
                });

        mockMvc.perform(get("/movies/list-with-filter-many-column-and-sortBy")
                        .param("keyword", "spider")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                // Note: sortBy param is not provided, so it will be null
                .andExpect(status().isOk());

        ArgumentCaptor<MovieFilterRequest> captor = ArgumentCaptor.forClass(MovieFilterRequest.class);
        verify(movieService, times(1)).getMoviesWithFilterBymanyColumnAndSortBy(captor.capture());
        
        // Verify that defaultSortBy "id:desc" is used when sortBy is null
        assertThat(captor.getValue().getSortBy()).isEqualTo("id:desc");
    }

    @Test
    @DisplayName("GET /movies/list-with-filter-many-column-and-sortBy with empty sortBy should use default sortBy")
    void getMoviesWithFilter_emptySortBy_shouldUseDefaultSortBy() throws Exception {
        // Test branch 1: sortBy.isEmpty() → use "id:desc"
        when(movieService.getMoviesWithFilterBymanyColumnAndSortBy(any(MovieFilterRequest.class)))
                .thenAnswer((Answer<PageResponse<?>>) invocation -> {
                    PageResponse<Object> response = PageResponse.<Object>builder()
                            .pageNo(1)
                            .pageSize(10)
                            .totalPages(1)
                            .totalItems(0L)
                            .items(null)
                            .build();
                    return (PageResponse<?>) response;
                });

        mockMvc.perform(get("/movies/list-with-filter-many-column-and-sortBy")
                        .param("keyword", "spider")
                        .param("sortBy", "")  // Empty string
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());

        ArgumentCaptor<MovieFilterRequest> captor = ArgumentCaptor.forClass(MovieFilterRequest.class);
        verify(movieService, times(1)).getMoviesWithFilterBymanyColumnAndSortBy(captor.capture());
        
        // Verify that defaultSortBy "id:desc" is used when sortBy is empty
        assertThat(captor.getValue().getSortBy()).isEqualTo("id:desc");
    }

    @Test
    @DisplayName("POST /movies/add should create movie successfully")
    void createMovie_shouldCreateSuccessfully() throws Exception {
        // Given
        Long expectedMovieId = 1L;
        
        // Create mock multipart file for poster
        MockMultipartFile posterFile = new MockMultipartFile(
                "poster",
                "test-poster.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        // Mock service to return movie ID
        when(movieService.create(any(MovieCreationRequest.class))).thenReturn(expectedMovieId);

        // When & Then
        mockMvc.perform(multipart("/movies/add")
                        .file(posterFile)
                        .param("name", "Test Movie")
                        .param("description", "This is a test movie description")
                        .param("duration", "120")
                        .param("releaseDate", "2024-12-25")
                        .param("director", "Test Director")
                        .param("actor", "Test Actor")
                        .param("ageRating", "13")
                        .param("trailerUrl", "https://www.youtube.com/watch?v=test")
                        .param("genreIds", "1", "2")
                        .param("languageId", "1")
                        .param("countryId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Movie created successfully"))
                .andExpect(jsonPath("$.data").value(expectedMovieId));

        // Verify service was called once with correct request
        ArgumentCaptor<MovieCreationRequest> captor = ArgumentCaptor.forClass(MovieCreationRequest.class);
        verify(movieService, times(1)).create(captor.capture());
        
        // Assert request data
        MovieCreationRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.getName()).isEqualTo("Test Movie");
        assertThat(capturedRequest.getDescription()).isEqualTo("This is a test movie description");
        assertThat(capturedRequest.getDuration()).isEqualTo(120);
        assertThat(capturedRequest.getDirector()).isEqualTo("Test Director");
        assertThat(capturedRequest.getActor()).isEqualTo("Test Actor");
        assertThat(capturedRequest.getLanguageId()).isEqualTo(1L);
        assertThat(capturedRequest.getCountryId()).isEqualTo(1L);
        assertThat(capturedRequest.getGenreIds()).containsExactly(1L, 2L);
        assertThat(capturedRequest.getPoster()).isNotNull();
    }

    @Test
    @DisplayName("POST /movies/add should return 400 when required fields are missing")
    void createMovie_shouldReturnBadRequestWhenFieldsMissing() throws Exception {
        // Create mock multipart file for poster
        MockMultipartFile posterFile = new MockMultipartFile(
                "poster",
                "test-poster.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        // Missing required fields (name, genreIds, etc.)
        mockMvc.perform(multipart("/movies/add")
                        .file(posterFile)
                        .param("description", "This is a test movie description")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        // Verify service was NOT called
        verify(movieService, never()).create(any());
    }

    @Test
    @DisplayName("POST /movies/add should return 400 when poster file is missing")
    void createMovie_shouldReturnBadRequestWhenPosterMissing() throws Exception {
        // Missing poster file
        mockMvc.perform(multipart("/movies/add")
                        .param("name", "Test Movie")
                        .param("description", "This is a test movie description")
                        .param("duration", "120")
                        .param("releaseDate", "2024-12-25")
                        .param("director", "Test Director")
                        .param("actor", "Test Actor")
                        .param("ageRating", "13")
                        .param("genreIds", "1", "2")
                        .param("languageId", "1")
                        .param("countryId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        // Verify service was NOT called
        verify(movieService, never()).create(any());
    }

    // ==================== PUT /movies/update-full/{movieId} ====================
    @Test
    @DisplayName("PUT /movies/update-full/{movieId} should update full movie successfully")
    void updateFullMovieById_shouldUpdateSuccessfully() throws Exception {
        // Create mock multipart files
        MockMultipartFile posterFile = new MockMultipartFile(
                "poster",
                "test-poster.jpg",
                "image/jpeg",
                "fake poster content".getBytes()
        );
        MockMultipartFile bannerFile = new MockMultipartFile(
                "banner",
                "test-banner.jpg",
                "image/jpeg",
                "fake banner content".getBytes()
        );

        doNothing().when(movieService).updateFullById(eq(1L), any(MovieUpdateFullRequest.class));

        // Use multipart with PUT method
        mockMvc.perform(multipart("/movies/update-full/1")
                        .file(posterFile)
                        .file(bannerFile)
                        .param("description", "Updated description")
                        .param("duration", "120")
                        .param("releaseDate", "2024-12-25")
                        .param("director", "Updated Director")
                        .param("actor", "Updated Actor")
                        .param("ageRating", "13")
                        .param("trailerUrl", "https://www.youtube.com/watch?v=updated")
                        .param("genreIds", "1", "2")
                        .param("languageId", "1")
                        .param("countryId", "1")
                        .param("status", "NOW_SHOWING")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Movie updated successfully"));

        ArgumentCaptor<MovieUpdateFullRequest> captor = ArgumentCaptor.forClass(MovieUpdateFullRequest.class);
        verify(movieService, times(1)).updateFullById(eq(1L), captor.capture());
        
        MovieUpdateFullRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.getDescription()).isEqualTo("Updated description");
        assertThat(capturedRequest.getDuration()).isEqualTo(120);
        assertThat(capturedRequest.getDirector()).isEqualTo("Updated Director");
        assertThat(capturedRequest.getActor()).isEqualTo("Updated Actor");
        assertThat(capturedRequest.getLanguageId()).isEqualTo(1L);
        assertThat(capturedRequest.getCountryId()).isEqualTo(1L);
        assertThat(capturedRequest.getGenreIds()).containsExactly(1L, 2L);
        assertThat(capturedRequest.getPoster()).isNotNull();
        assertThat(capturedRequest.getBanner()).isNotNull();
    }

    // ==================== POST /movies/search ====================
    @Test
    @DisplayName("POST /movies/search should return movie list for booking")
    void getMovieListToBooking_shouldReturnMovieList() throws Exception {
        // Create UserSearchMovieRequest using reflection or JSON
        // Since UserSearchMovieRequest only has @Getter, we'll create it via JSON
        // MovieStatus enum values: UPCOMING, PLAYING, ENDED
        String jsonRequest = """
                {
                    "name": "Spider",
                    "genreId": 1,
                    "status": "PLAYING",
                    "pageNo": 1,
                    "pageSize": 10
                }
                """;

        // Mock PageResponse
        when(movieService.getMovieListToBooking(any(UserSearchMovieRequest.class)))
                .thenAnswer((Answer<PageResponse<?>>) invocation -> {
                    PageResponse<Object> response = PageResponse.<Object>builder()
                            .pageNo(1)
                            .pageSize(10)
                            .totalPages(1)
                            .totalItems(0L)
                            .items(null)
                            .build();
                    return (PageResponse<?>) response;
                });

        mockMvc.perform(post("/movies/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get movie list to booking successfully"))
                .andExpect(jsonPath("$.data").exists());

        verify(movieService, times(1)).getMovieListToBooking(any(UserSearchMovieRequest.class));
    }

    // ==================== GET /movies/with-showtimes/{date} ====================
    @Test
    @DisplayName("GET /movies/with-showtimes/{date} should return movies with showtimes on date")
    void getMoviesWithShowtimesOnDate_shouldReturnMoviesList() throws Exception {
        LocalDate testDate = LocalDate.of(2024, 12, 25);
        List<StaffMovieListResponse> movies = Arrays.asList(
                StaffMovieListResponse.builder()
                        .id(1L)
                        .name("Test Movie")
                        .posterUrl("http://example.com/poster.jpg")
                        .duration(120)
                        .ageRating(13)
                        .build()
        );

        when(movieService.getMoviesWithShowtimesOnDate(eq(testDate), eq("test")))
                .thenReturn(movies);

        mockMvc.perform(get("/movies/with-showtimes/2024-12-25")
                        .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get movies with showtimes successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Test Movie"))
                .andExpect(jsonPath("$.data[0].duration").value(120));

        verify(movieService, times(1)).getMoviesWithShowtimesOnDate(eq(testDate), eq("test"));
    }

    @Test
    @DisplayName("GET /movies/with-showtimes/{date} without keyword should return movies")
    void getMoviesWithShowtimesOnDate_withoutKeyword_shouldReturnMovies() throws Exception {
        LocalDate testDate = LocalDate.of(2024, 12, 25);
        List<StaffMovieListResponse> movies = Arrays.asList(
                StaffMovieListResponse.builder()
                        .id(1L)
                        .name("Test Movie")
                        .build()
        );

        when(movieService.getMoviesWithShowtimesOnDate(eq(testDate), eq(null)))
                .thenReturn(movies);

        mockMvc.perform(get("/movies/with-showtimes/2024-12-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(movieService, times(1)).getMoviesWithShowtimesOnDate(eq(testDate), eq(null));
    }
}

