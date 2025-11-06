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
import vn.cineshow.dto.request.room.RoomRequest;
import vn.cineshow.dto.response.PageResponse;
import vn.cineshow.dto.response.room.RoomDTO;
import vn.cineshow.dto.response.room.RoomMetaResponse;
import vn.cineshow.dto.response.room.room_type.RoomTypeDTO;
import vn.cineshow.dto.response.seat.seat_type.SeatTypeDTO;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.RoomService;
import vn.cineshow.service.RoomTypeService;
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

@WebMvcTest(controllers = RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    @MockBean
    private RoomTypeService roomTypeService;

    @MockBean
    private SeatTypeService seatTypeService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AccountDetailsService accountDetailsService;

    // ==================== GET /rooms/meta ====================
    @Test
    @DisplayName("GET /rooms/meta should return metadata successfully")
    void getMeta_shouldReturnMetadataSuccessfully() throws Exception {
        List<RoomTypeDTO> roomTypes = Arrays.asList(
                RoomTypeDTO.builder().id(1L).name("2D").build()
        );
        List<SeatTypeDTO> seatTypes = Arrays.asList(
                SeatTypeDTO.builder().id(1L).name("Normal").build()
        );

        when(roomTypeService.getAllRoomTypesDTO()).thenReturn(roomTypes);
        when(seatTypeService.getAllSeatTypesDTO()).thenReturn(seatTypes);

        mockMvc.perform(get("/rooms/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Lấy metadata thành công"))
                .andExpect(jsonPath("$.data.roomTypes").isArray())
                .andExpect(jsonPath("$.data.seatTypes").isArray());

        verify(roomTypeService, times(1)).getAllRoomTypesDTO();
        verify(seatTypeService, times(1)).getAllSeatTypesDTO();
    }

    // ==================== GET /rooms ====================
    @Test
    @DisplayName("GET /rooms should return rooms list successfully")
    void searchRooms_shouldReturnRoomsList() throws Exception {
        RoomDTO room = RoomDTO.builder()
                .id(1L)
                .name("Phòng 1")
                .build();
        PageResponse<List<RoomDTO>> pageResponse = PageResponse.<List<RoomDTO>>builder()
                .items(Arrays.asList(room))
                .pageNo(1)
                .pageSize(10)
                .totalItems(1L)
                .totalPages(1)
                .build();

        when(roomService.searchRooms(any(), any(), any(), any(), any(), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/rooms")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Lấy danh sách phòng chiếu thành công"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].id").value(1L));

        verify(roomService, times(1)).searchRooms(any(), any(), any(), any(), any(), any());
    }

    // ==================== GET /rooms/{roomId} ====================
    @Test
    @DisplayName("GET /rooms/{roomId} should return room detail successfully")
    void getRoomDetail_shouldReturnRoomSuccessfully() throws Exception {
        RoomDTO room = RoomDTO.builder()
                .id(1L)
                .name("Phòng 1")
                .build();

        when(roomService.getRoomDetail(1L)).thenReturn(room);

        mockMvc.perform(get("/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Lấy thông tin phòng chiếu thành công"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Phòng 1"));

        verify(roomService, times(1)).getRoomDetail(1L);
    }

    @Test
    @DisplayName("GET /rooms/{roomId} should return 404 when room not found")
    void getRoomDetail_notFound_shouldReturnNotFound() throws Exception {
        when(roomService.getRoomDetail(999L)).thenReturn(null);

        mockMvc.perform(get("/rooms/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Không tìm thấy phòng chiếu"));

        verify(roomService, times(1)).getRoomDetail(999L);
    }

    // ==================== POST /rooms ====================
    @Test
    @DisplayName("POST /rooms should create room successfully")
    void createRoom_shouldCreateSuccessfully() throws Exception {
        RoomRequest request = RoomRequest.builder()
                .name("Phòng mới")
                .roomTypeId(1L)
                .rows(10)
                .columns(15)
                .status("ACTIVE")
                .description("Phòng chiếu mới")
                .build();

        RoomDTO created = RoomDTO.builder()
                .id(1L)
                .name("Phòng mới")
                .build();

        when(roomService.createRoom(any(RoomRequest.class))).thenReturn(created);

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Đã thêm phòng chiếu thành công"))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(roomService, times(1)).createRoom(any(RoomRequest.class));
    }

    // ==================== PUT /rooms/{roomId} ====================
    @Test
    @DisplayName("PUT /rooms/{roomId} should update room successfully")
    void updateRoom_shouldUpdateSuccessfully() throws Exception {
        RoomRequest request = RoomRequest.builder()
                .name("Phòng đã cập nhật")
                .roomTypeId(1L)
                .rows(10)
                .columns(15)
                .status("ACTIVE")
                .description("Phòng đã cập nhật")
                .build();

        RoomDTO updated = RoomDTO.builder()
                .id(1L)
                .name("Phòng đã cập nhật")
                .build();

        when(roomService.updateRoom(eq(1L), any(RoomRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/rooms/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Đã cập nhật phòng chiếu thành công"))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(roomService, times(1)).updateRoom(eq(1L), any(RoomRequest.class));
    }

    @Test
    @DisplayName("PUT /rooms/{roomId} should return 404 when room not found")
    void updateRoom_notFound_shouldReturnNotFound() throws Exception {
        RoomRequest request = RoomRequest.builder()
                .name("Phòng đã cập nhật")
                .roomTypeId(1L)
                .rows(10)
                .columns(15)
                .status("ACTIVE")
                .description("Phòng đã cập nhật")
                .build();

        when(roomService.updateRoom(eq(999L), any(RoomRequest.class))).thenReturn(null);

        mockMvc.perform(put("/rooms/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Không tìm thấy phòng chiếu để cập nhật"));

        verify(roomService, times(1)).updateRoom(eq(999L), any(RoomRequest.class));
    }

    // ==================== DELETE /rooms/{roomId} ====================
    @Test
    @DisplayName("DELETE /rooms/{roomId} should delete room successfully")
    void deleteRoom_shouldDeleteSuccessfully() throws Exception {
        when(roomService.deleteRoom(1L)).thenReturn(true);

        mockMvc.perform(delete("/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Đã xóa phòng chiếu thành công"));

        verify(roomService, times(1)).deleteRoom(1L);
    }

    @Test
    @DisplayName("DELETE /rooms/{roomId} should return 404 when room not found")
    void deleteRoom_notFound_shouldReturnNotFound() throws Exception {
        when(roomService.deleteRoom(999L)).thenReturn(false);

        mockMvc.perform(delete("/rooms/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Không tìm thấy phòng chiếu để xóa"));

        verify(roomService, times(1)).deleteRoom(999L);
    }
}

