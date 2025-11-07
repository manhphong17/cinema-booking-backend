package vn.cineshow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.cineshow.dto.request.showtime.CreateShowTimeRequest;
import vn.cineshow.dto.request.showtime.UpdateShowTimeRequest;
import vn.cineshow.dto.response.IdNameDTO;
import vn.cineshow.dto.response.showtime.ShowTimeListDTO;
import vn.cineshow.dto.response.showtime.ShowTimeResponse;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.ShowTimeService;
import vn.cineshow.service.impl.AccountDetailsService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ShowTimeController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShowTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShowTimeService showTimeService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AccountDetailsService accountDetailsService;

    // ==================== GET /api/showtimes/lookup/id-name-movies ====================
    @Test
    @DisplayName("GET /api/showtimes/lookup/id-name-movies should return movies list successfully")
    void lookupIdNameMoviesForShowtime_shouldReturnMoviesList() throws Exception {
        IdNameDTO movie = new IdNameDTO(1L, "Test Movie");
        List<IdNameDTO> movies = Arrays.asList(movie);

        when(showTimeService.getIdNameMoviesPlayingAndUpcoming()).thenReturn(movies);

        mockMvc.perform(get("/api/showtimes/lookup/id-name-movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Movies retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Test Movie"));

        verify(showTimeService, times(1)).getIdNameMoviesPlayingAndUpcoming();
    }

    // ==================== GET /api/showtimes/all ====================
    @Test
    @DisplayName("GET /api/showtimes/all should return paginated showtimes successfully")
    void getAll_shouldReturnPaginatedShowtimes() throws Exception {
        ShowTimeListDTO dto = new ShowTimeListDTO(1L, 1L, "Test Movie", null, 1L, "2D", 1L, "Phòng 1", 1L, "Vietnamese", "2025-01-01 14:00", "2025-01-01 16:00");
        List<ShowTimeListDTO> showtimes = Arrays.asList(dto);
        Page<ShowTimeListDTO> page = new PageImpl<>(showtimes, PageRequest.of(0, 10, Sort.by("startTime").ascending()), 1);

        when(showTimeService.getAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/showtimes/all")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "startTime,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].showtimeId").value(1L));

        verify(showTimeService, times(1)).getAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("GET /api/showtimes/all with sort length != 2 should use default sort")
    void getAll_withInvalidSort_shouldUseDefaultSort() throws Exception {
        ShowTimeListDTO dto = new ShowTimeListDTO(1L, 1L, "Test Movie", null, 1L, "2D", 1L, "Phòng 1", 1L, "Vietnamese", "2025-01-01 14:00", "2025-01-01 16:00");
        List<ShowTimeListDTO> showtimes = Arrays.asList(dto);
        // When sort length != 2, it uses default Sort.by("startTime").ascending()
        Page<ShowTimeListDTO> page = new PageImpl<>(showtimes, PageRequest.of(0, 10, Sort.by("startTime").ascending()), 1);

        when(showTimeService.getAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        // Test with sort string that doesn't have 2 parts (length != 2)
        mockMvc.perform(get("/api/showtimes/all")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "startTime")) // Only one part, length != 2
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].showtimeId").value(1L));

        verify(showTimeService, times(1)).getAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("GET /api/showtimes/all with sort descending should work correctly")
    void getAll_withSortDescending_shouldWork() throws Exception {
        ShowTimeListDTO dto = new ShowTimeListDTO(1L, 1L, "Test Movie", null, 1L, "2D", 1L, "Phòng 1", 1L, "Vietnamese", "2025-01-01 14:00", "2025-01-01 16:00");
        List<ShowTimeListDTO> showtimes = Arrays.asList(dto);
        Page<ShowTimeListDTO> page = new PageImpl<>(showtimes, PageRequest.of(0, 10, Sort.by("startTime").descending()), 1);

        when(showTimeService.getAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/showtimes/all")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "startTime,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].showtimeId").value(1L));

        verify(showTimeService, times(1)).getAll(any(org.springframework.data.domain.Pageable.class));
    }

    // ==================== GET /api/showtimes ====================
    @Test
    @DisplayName("GET /api/showtimes should return all showtimes successfully")
    void getAllPlain_shouldReturnAllShowtimes() throws Exception {
        ShowTimeListDTO dto = new ShowTimeListDTO(1L, 1L, "Test Movie", null, 1L, "2D", 1L, "Phòng 1", 1L, "Vietnamese", "2025-01-01 14:00", "2025-01-01 16:00");
        List<ShowTimeListDTO> showtimes = Arrays.asList(dto);

        when(showTimeService.getAllPlain()).thenReturn(showtimes);

        mockMvc.perform(get("/api/showtimes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].showtimeId").value(1L));

        verify(showTimeService, times(1)).getAllPlain();
    }

    // ==================== GET /api/showtimes/filter ====================
    @Test
    @DisplayName("GET /api/showtimes/filter should filter showtimes successfully")
    void filterShowtimes_shouldFilterSuccessfully() throws Exception {
        ShowTimeListDTO dto = new ShowTimeListDTO(1L, 1L, "Test Movie", null, 1L, "2D", 1L, "Phòng 1", 1L, "Vietnamese", "2025-01-01 14:00", "2025-01-01 16:00");
        List<ShowTimeListDTO> showtimes = Arrays.asList(dto);

        when(showTimeService.findShowtimes(any(), any(), any(), any(), any(), any())).thenReturn(showtimes);

        mockMvc.perform(get("/api/showtimes/filter")
                        .param("movieId", "1")
                        .param("date", "2025-01-01")
                        .param("roomId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Showtimes filtered successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].showtimeId").value(1L));

        verify(showTimeService, times(1)).findShowtimes(any(), any(), any(), any(), any(), any());
    }

    // ==================== GET /api/showtimes/rooms/lookup/id-name ====================
    @Test
    @DisplayName("GET /api/showtimes/rooms/lookup/id-name should return rooms list successfully")
    void getAllRoomsIdName_shouldReturnRoomsList() throws Exception {
        IdNameDTO room = new IdNameDTO(1L, "Phòng 1");
        List<IdNameDTO> rooms = Arrays.asList(room);

        when(showTimeService.getAllRoomsIdName()).thenReturn(rooms);

        mockMvc.perform(get("/api/showtimes/rooms/lookup/id-name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Rooms retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Phòng 1"));

        verify(showTimeService, times(1)).getAllRoomsIdName();
    }

    // ==================== GET /api/showtimes/subtitles/lookup/id-name ====================
    @Test
    @DisplayName("GET /api/showtimes/subtitles/lookup/id-name should return subtitles list successfully")
    void getAllSubTitlesIdName_shouldReturnSubtitlesList() throws Exception {
        IdNameDTO subtitle = new IdNameDTO(1L, "Vietnamese");
        List<IdNameDTO> subtitles = Arrays.asList(subtitle);

        when(showTimeService.getAllSubTitlesIdName()).thenReturn(subtitles);

        mockMvc.perform(get("/api/showtimes/subtitles/lookup/id-name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Subtitles retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L));

        verify(showTimeService, times(1)).getAllSubTitlesIdName();
    }

    // ==================== GET /api/showtimes/room-types/lookup/id-name ====================
    @Test
    @DisplayName("GET /api/showtimes/room-types/lookup/id-name should return room types list successfully")
    void getAllRoomTypesIdName_shouldReturnRoomTypesList() throws Exception {
        IdNameDTO roomType = new IdNameDTO(1L, "2D");
        List<IdNameDTO> roomTypes = Arrays.asList(roomType);

        when(showTimeService.getAllRoomTypesIdName()).thenReturn(roomTypes);

        mockMvc.perform(get("/api/showtimes/room-types/lookup/id-name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Room types retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L));

        verify(showTimeService, times(1)).getAllRoomTypesIdName();
    }

    // ==================== POST /api/showtimes/createShowtime ====================
    @Test
    @DisplayName("POST /api/showtimes/createShowtime should create showtime successfully")
    void create_shouldCreateSuccessfully() throws Exception {
        CreateShowTimeRequest request = CreateShowTimeRequest.builder()
                .movieId(1L)
                .roomId(1L)
                .subtitleId(1L)
                .startTime(LocalDateTime.of(2025, 1, 1, 14, 0))
                .endTime(LocalDateTime.of(2025, 1, 1, 16, 0))
                .build();

        ShowTimeResponse response = ShowTimeResponse.builder()
                .id(1L)
                .movieId(1L)
                .roomId(1L)
                .subtitleId(1L)
                .startTime(LocalDateTime.of(2025, 1, 1, 14, 0))
                .endTime(LocalDateTime.of(2025, 1, 1, 16, 0))
                .build();

        when(showTimeService.createShowTime(any(CreateShowTimeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/showtimes/createShowtime")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Create sucess"))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(showTimeService, times(1)).createShowTime(any(CreateShowTimeRequest.class));
    }

    // ==================== GET /api/showtimes/showtimeBy/{id} ====================
    @Test
    @DisplayName("GET /api/showtimes/showtimeBy/{id} should return showtime successfully")
    void getShowtimeById_shouldReturnShowtime() throws Exception {
        ShowTimeListDTO dto = new ShowTimeListDTO(1L, 1L, "Test Movie", null, 1L, "2D", 1L, "Phòng 1", 1L, "Vietnamese", "2025-01-01 14:00", "2025-01-01 16:00");

        when(showTimeService.getShowTimeById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/showtimes/showtimeBy/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(showTimeService, times(1)).getShowTimeById(1L);
    }

    // ==================== PUT /api/showtimes/{id} ====================
    @Test
    @DisplayName("PUT /api/showtimes/{id} should update showtime successfully")
    void updatePut_shouldUpdateSuccessfully() throws Exception {
        UpdateShowTimeRequest request = UpdateShowTimeRequest.builder()
                .movieId(1L)
                .roomId(1L)
                .subtitleId(1L)
                .startTime(LocalDateTime.of(2025, 1, 1, 14, 0))
                .endTime(LocalDateTime.of(2025, 1, 1, 16, 0))
                .build();

        ShowTimeResponse response = ShowTimeResponse.builder()
                .id(1L)
                .movieId(1L)
                .roomId(1L)
                .subtitleId(1L)
                .startTime(LocalDateTime.of(2025, 1, 1, 14, 0))
                .endTime(LocalDateTime.of(2025, 1, 1, 16, 0))
                .build();

        when(showTimeService.updateShowTime(eq(1L), any(UpdateShowTimeRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/showtimes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(showTimeService, times(1)).updateShowTime(eq(1L), any(UpdateShowTimeRequest.class));
    }

    // ==================== PATCH /api/showtimes/{id} ====================
    @Test
    @DisplayName("PATCH /api/showtimes/{id} should patch showtime successfully")
    void updatePatch_shouldPatchSuccessfully() throws Exception {
        UpdateShowTimeRequest request = UpdateShowTimeRequest.builder()
                .movieId(1L)
                .roomId(1L)
                .subtitleId(1L)
                .startTime(LocalDateTime.of(2025, 1, 1, 14, 0))
                .endTime(LocalDateTime.of(2025, 1, 1, 16, 0))
                .build();

        ShowTimeResponse response = ShowTimeResponse.builder()
                .id(1L)
                .movieId(1L)
                .roomId(1L)
                .subtitleId(1L)
                .startTime(LocalDateTime.of(2025, 1, 1, 14, 0))
                .endTime(LocalDateTime.of(2025, 1, 1, 16, 0))
                .build();

        when(showTimeService.updateShowTime(eq(1L), any(UpdateShowTimeRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/showtimes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(showTimeService, times(1)).updateShowTime(eq(1L), any(UpdateShowTimeRequest.class));
    }

    // ==================== DELETE /api/showtimes/{id} ====================
    @Test
    @DisplayName("DELETE /api/showtimes/{id} should soft delete showtime successfully")
    void softDelete_shouldDeleteSuccessfully() throws Exception {
        doNothing().when(showTimeService).softDelete(1L);

        mockMvc.perform(delete("/api/showtimes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Delete successfully"));

        verify(showTimeService, times(1)).softDelete(1L);
    }

    // ==================== POST /api/showtimes/{id}/restore ====================
    @Test
    @DisplayName("POST /api/showtimes/{id}/restore should restore showtime successfully")
    void restore_shouldRestoreSuccessfully() throws Exception {
        doNothing().when(showTimeService).restore(1L);

        mockMvc.perform(post("/api/showtimes/1/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Restored successfully"));

        verify(showTimeService, times(1)).restore(1L);
    }
}

