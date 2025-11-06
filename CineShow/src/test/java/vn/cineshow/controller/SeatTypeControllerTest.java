package vn.cineshow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.cineshow.dto.request.seat.SeatTypeCreateRequest;
import vn.cineshow.dto.request.seat.SeatTypeUpdateRequest;
import vn.cineshow.dto.response.seat.seat_type.SeatTypeResponse;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.SeatTypeService;
import vn.cineshow.service.impl.AccountDetailsService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SeatTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
class SeatTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SeatTypeService seatTypeService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AccountDetailsService accountDetailsService;

    // ==================== GET /api/seat-types ====================
    @Test
    @DisplayName("GET /api/seat-types should return seat types list successfully")
    void list_shouldReturnSeatTypesList() throws Exception {
        SeatTypeResponse dto = SeatTypeResponse.builder()
                .id(1L)
                .name("Normal")
                .build();
        List<SeatTypeResponse> types = Arrays.asList(dto);

        when(seatTypeService.findAll(null)).thenReturn(types);

        mockMvc.perform(get("/api/seat-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Normal"));

        verify(seatTypeService, times(1)).findAll(null);
    }

    // ==================== GET /api/seat-types/{id} ====================
    @Test
    @DisplayName("GET /api/seat-types/{id} should return seat type successfully")
    void get_shouldReturnSeatType() throws Exception {
        SeatTypeResponse dto = SeatTypeResponse.builder()
                .id(1L)
                .name("Normal")
                .build();

        when(seatTypeService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/seat-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Normal"));

        verify(seatTypeService, times(1)).findById(1L);
    }

    // ==================== POST /api/seat-types ====================
    @Test
    @DisplayName("POST /api/seat-types should create seat type successfully")
    void create_shouldCreateSuccessfully() throws Exception {
        SeatTypeCreateRequest request = SeatTypeCreateRequest.builder()
                .name("VIP")
                .build();

        SeatTypeResponse created = SeatTypeResponse.builder()
                .id(1L)
                .name("VIP")
                .build();

        when(seatTypeService.create(any(SeatTypeCreateRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/seat-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("VIP"));

        verify(seatTypeService, times(1)).create(any(SeatTypeCreateRequest.class));
    }

    // ==================== PUT /api/seat-types/{id} ====================
    @Test
    @DisplayName("PUT /api/seat-types/{id} should update seat type successfully")
    void update_shouldUpdateSuccessfully() throws Exception {
        SeatTypeUpdateRequest request = SeatTypeUpdateRequest.builder()
                .name("Updated Normal")
                .build();

        SeatTypeResponse updated = SeatTypeResponse.builder()
                .id(1L)
                .name("Updated Normal")
                .build();

        when(seatTypeService.update(eq(1L), any(SeatTypeUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/seat-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Normal"));

        verify(seatTypeService, times(1)).update(eq(1L), any(SeatTypeUpdateRequest.class));
    }

    // ==================== PATCH /api/seat-types/{id} ====================
    @Test
    @DisplayName("PATCH /api/seat-types/{id} should patch seat type successfully")
    void patch_shouldPatchSuccessfully() throws Exception {
        SeatTypeUpdateRequest request = SeatTypeUpdateRequest.builder()
                .name("Patched Normal")
                .build();

        SeatTypeResponse patched = SeatTypeResponse.builder()
                .id(1L)
                .name("Patched Normal")
                .build();

        when(seatTypeService.patch(eq(1L), any(SeatTypeUpdateRequest.class))).thenReturn(patched);

        mockMvc.perform(patch("/api/seat-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Patched Normal"));

        verify(seatTypeService, times(1)).patch(eq(1L), any(SeatTypeUpdateRequest.class));
    }

    // ==================== DELETE /api/seat-types/{id} ====================
    @Test
    @DisplayName("DELETE /api/seat-types/{id} should delete seat type successfully")
    void delete_shouldDeleteSuccessfully() throws Exception {
        doNothing().when(seatTypeService).delete(1L);

        mockMvc.perform(delete("/api/seat-types/1"))
                .andExpect(status().isNoContent());

        verify(seatTypeService, times(1)).delete(1L);
    }

    // ==================== POST /api/seat-types/{id}/activate ====================
    @Test
    @DisplayName("POST /api/seat-types/{id}/activate should activate seat type successfully")
    void activate_shouldActivateSuccessfully() throws Exception {
        SeatTypeResponse activated = SeatTypeResponse.builder()
                .id(1L)
                .name("Normal")
                .build();

        when(seatTypeService.activate(1L)).thenReturn(activated);

        mockMvc.perform(post("/api/seat-types/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(seatTypeService, times(1)).activate(1L);
    }

    // ==================== POST /api/seat-types/{id}/deactivate ====================
    @Test
    @DisplayName("POST /api/seat-types/{id}/deactivate should deactivate seat type successfully")
    void deactivate_shouldDeactivateSuccessfully() throws Exception {
        SeatTypeResponse deactivated = SeatTypeResponse.builder()
                .id(1L)
                .name("Normal")
                .build();

        when(seatTypeService.deactivate(1L)).thenReturn(deactivated);

        mockMvc.perform(post("/api/seat-types/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(seatTypeService, times(1)).deactivate(1L);
    }
}

