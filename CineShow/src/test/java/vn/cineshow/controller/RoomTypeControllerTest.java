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
import vn.cineshow.dto.request.room.RoomTypeCreateRequest;
import vn.cineshow.dto.request.room.RoomTypeUpdateRequest;
import vn.cineshow.dto.response.room.room_type.RoomTypeResponse;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.RoomTypeService;
import vn.cineshow.service.impl.AccountDetailsService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RoomTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomTypeService roomTypeService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AccountDetailsService accountDetailsService;

    // ==================== GET /api/room-types ====================
    @Test
    @DisplayName("GET /api/room-types should return room types list successfully")
    void list_shouldReturnRoomTypesList() throws Exception {
        RoomTypeResponse dto = RoomTypeResponse.builder()
                .id(1L)
                .name("2D")
                .build();
        List<RoomTypeResponse> types = Arrays.asList(dto);

        when(roomTypeService.findAll(null)).thenReturn(types);

        mockMvc.perform(get("/api/room-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("2D"));

        verify(roomTypeService, times(1)).findAll(null);
    }

    @Test
    @DisplayName("GET /api/room-types with onlyActive should return filtered list")
    void list_withOnlyActive_shouldReturnFilteredList() throws Exception {
        RoomTypeResponse dto = RoomTypeResponse.builder()
                .id(1L)
                .name("2D")
                .build();
        List<RoomTypeResponse> types = Arrays.asList(dto);

        when(roomTypeService.findAll(true)).thenReturn(types);

        mockMvc.perform(get("/api/room-types")
                        .param("onlyActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(roomTypeService, times(1)).findAll(true);
    }

    // ==================== GET /api/room-types/{id} ====================
    @Test
    @DisplayName("GET /api/room-types/{id} should return room type successfully")
    void get_shouldReturnRoomType() throws Exception {
        RoomTypeResponse dto = RoomTypeResponse.builder()
                .id(1L)
                .name("2D")
                .build();

        when(roomTypeService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/room-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("2D"));

        verify(roomTypeService, times(1)).findById(1L);
    }

    // ==================== POST /api/room-types ====================
    @Test
    @DisplayName("POST /api/room-types should create room type successfully")
    void create_shouldCreateSuccessfully() throws Exception {
        RoomTypeCreateRequest request = RoomTypeCreateRequest.builder()
                .name("3D")
                .build();

        RoomTypeResponse created = RoomTypeResponse.builder()
                .id(1L)
                .name("3D")
                .build();

        when(roomTypeService.create(any(RoomTypeCreateRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/room-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("3D"));

        verify(roomTypeService, times(1)).create(any(RoomTypeCreateRequest.class));
    }

    // ==================== PUT /api/room-types/{id} ====================
    @Test
    @DisplayName("PUT /api/room-types/{id} should update room type successfully")
    void update_shouldUpdateSuccessfully() throws Exception {
        RoomTypeUpdateRequest request = RoomTypeUpdateRequest.builder()
                .name("Updated 2D")
                .build();

        RoomTypeResponse updated = RoomTypeResponse.builder()
                .id(1L)
                .name("Updated 2D")
                .build();

        when(roomTypeService.update(eq(1L), any(RoomTypeUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/room-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated 2D"));

        verify(roomTypeService, times(1)).update(eq(1L), any(RoomTypeUpdateRequest.class));
    }

    // ==================== PATCH /api/room-types/{id} ====================
    @Test
    @DisplayName("PATCH /api/room-types/{id} should patch room type successfully")
    void patch_shouldPatchSuccessfully() throws Exception {
        RoomTypeUpdateRequest request = RoomTypeUpdateRequest.builder()
                .name("Patched 2D")
                .build();

        RoomTypeResponse patched = RoomTypeResponse.builder()
                .id(1L)
                .name("Patched 2D")
                .build();

        when(roomTypeService.patch(eq(1L), any(RoomTypeUpdateRequest.class))).thenReturn(patched);

        mockMvc.perform(patch("/api/room-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Patched 2D"));

        verify(roomTypeService, times(1)).patch(eq(1L), any(RoomTypeUpdateRequest.class));
    }

    // ==================== DELETE /api/room-types/{id} ====================
    @Test
    @DisplayName("DELETE /api/room-types/{id} should delete room type successfully")
    void delete_shouldDeleteSuccessfully() throws Exception {
        doNothing().when(roomTypeService).delete(1L);

        mockMvc.perform(delete("/api/room-types/1"))
                .andExpect(status().isNoContent());

        verify(roomTypeService, times(1)).delete(1L);
    }

    // ==================== POST /api/room-types/{id}/activate ====================
    @Test
    @DisplayName("POST /api/room-types/{id}/activate should activate room type successfully")
    void activate_shouldActivateSuccessfully() throws Exception {
        RoomTypeResponse activated = RoomTypeResponse.builder()
                .id(1L)
                .name("2D")
                .build();

        when(roomTypeService.activate(1L)).thenReturn(activated);

        mockMvc.perform(post("/api/room-types/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(roomTypeService, times(1)).activate(1L);
    }

    // ==================== POST /api/room-types/{id}/deactivate ====================
    @Test
    @DisplayName("POST /api/room-types/{id}/deactivate should deactivate room type successfully")
    void deactivate_shouldDeactivateSuccessfully() throws Exception {
        RoomTypeResponse deactivated = RoomTypeResponse.builder()
                .id(1L)
                .name("2D")
                .build();

        when(roomTypeService.deactivate(1L)).thenReturn(deactivated);

        mockMvc.perform(post("/api/room-types/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(roomTypeService, times(1)).deactivate(1L);
    }
}

