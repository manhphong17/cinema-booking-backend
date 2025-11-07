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
import vn.cineshow.dto.request.seat.BulkBlockRequest;
import vn.cineshow.dto.request.seat.BulkTypeRequest;
import vn.cineshow.dto.request.seat.SeatInitRequest;
import vn.cineshow.dto.request.seat.SeatMatrixRequest;
import vn.cineshow.dto.response.seat.SeatMatrixResponse;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.SeatService;
import vn.cineshow.service.impl.AccountDetailsService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SeatController.class)
@AutoConfigureMockMvc(addFilters = false)
class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SeatService seatService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AccountDetailsService accountDetailsService;

    // ==================== POST /rooms/{roomId}/seats/init ====================
    @Test
    @DisplayName("POST /rooms/{roomId}/seats/init should initialize seats successfully")
    void initSeats_shouldInitializeSuccessfully() throws Exception {
        SeatInitRequest request = SeatInitRequest.builder()
                .rows(10)
                .columns(15)
                .defaultSeatTypeId(1L)
                .build();

        when(seatService.initSeats(eq(1L), any(SeatInitRequest.class))).thenReturn(150);

        mockMvc.perform(post("/rooms/1/seats/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Khởi tạo ghế thành công"))
                .andExpect(jsonPath("$.data.created").value(150));

        verify(seatService, times(1)).initSeats(eq(1L), any(SeatInitRequest.class));
    }

    // ==================== GET /rooms/{roomId}/seats/matrix ====================
    @Test
    @DisplayName("GET /rooms/{roomId}/seats/matrix should return seat matrix successfully")
    void getSeatMatrix_shouldReturnMatrixSuccessfully() throws Exception {
        SeatMatrixResponse matrix = SeatMatrixResponse.builder()
                .room(null)
                .matrix(null)
                .build();

        when(seatService.getSeatMatrix(1L)).thenReturn(matrix);

        mockMvc.perform(get("/rooms/1/seats/matrix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Lấy ma trận ghế thành công"))
                .andExpect(jsonPath("$.data").exists());

        verify(seatService, times(1)).getSeatMatrix(1L);
    }

    @Test
    @DisplayName("GET /rooms/{roomId}/seats/matrix should return 404 when room not found")
    void getSeatMatrix_notFound_shouldReturnNotFound() throws Exception {
        when(seatService.getSeatMatrix(999L)).thenReturn(null);

        mockMvc.perform(get("/rooms/999/seats/matrix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Không tìm thấy phòng hoặc ma trận ghế"));

        verify(seatService, times(1)).getSeatMatrix(999L);
    }

    // ==================== PUT /rooms/{roomId}/seats/matrix ====================
    @Test
    @DisplayName("PUT /rooms/{roomId}/seats/matrix should save seat matrix successfully")
    void saveSeatMatrix_shouldSaveSuccessfully() throws Exception {
        SeatMatrixRequest request = SeatMatrixRequest.builder()
                .matrix(null)
                .build();

        Map<String, Integer> result = new HashMap<>();
        result.put("updated", 10);
        result.put("created", 5);
        result.put("deleted", 2);

        when(seatService.saveSeatMatrix(eq(1L), any(SeatMatrixRequest.class))).thenReturn(result);

        mockMvc.perform(put("/rooms/1/seats/matrix")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Lưu cấu hình ghế thành công"))
                .andExpect(jsonPath("$.data.updated").value(10))
                .andExpect(jsonPath("$.data.created").value(5))
                .andExpect(jsonPath("$.data.deleted").value(2));

        verify(seatService, times(1)).saveSeatMatrix(eq(1L), any(SeatMatrixRequest.class));
    }

    // ==================== PATCH /rooms/{roomId}/seats/bulk-type ====================
    @Test
    @DisplayName("PATCH /rooms/{roomId}/seats/bulk-type should update seat types successfully")
    void bulkUpdateSeatType_shouldUpdateSuccessfully() throws Exception {
        BulkTypeRequest request = BulkTypeRequest.builder()
                .targets(null)
                .seatTypeId(2L)
                .build();

        when(seatService.bulkUpdateSeatType(eq(1L), any(BulkTypeRequest.class))).thenReturn(10);

        mockMvc.perform(patch("/rooms/1/seats/bulk-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Cập nhật loại ghế hàng loạt thành công"))
                .andExpect(jsonPath("$.data.affected").value(10));

        verify(seatService, times(1)).bulkUpdateSeatType(eq(1L), any(BulkTypeRequest.class));
    }

    // ==================== PATCH /rooms/{roomId}/seats/bulk-block ====================
    @Test
    @DisplayName("PATCH /rooms/{roomId}/seats/bulk-block should block seats successfully")
    void bulkBlockSeats_shouldBlockSuccessfully() throws Exception {
        BulkBlockRequest request = BulkBlockRequest.builder()
                .targets(null)
                .blocked(true)
                .build();

        when(seatService.bulkBlockSeats(eq(1L), any(BulkBlockRequest.class))).thenReturn(5);

        mockMvc.perform(patch("/rooms/1/seats/bulk-block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Cập nhật trạng thái khóa ghế hàng loạt thành công"))
                .andExpect(jsonPath("$.data.affected").value(5));

        verify(seatService, times(1)).bulkBlockSeats(eq(1L), any(BulkBlockRequest.class));
    }
}


