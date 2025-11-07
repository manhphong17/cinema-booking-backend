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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import vn.cineshow.dto.request.concession.ConcessionAddRequest;
import vn.cineshow.dto.request.concession.ConcessionTypeRequest;
import vn.cineshow.dto.request.concession.ConcessionUpdateRequest;
import vn.cineshow.dto.response.concession.ConcessionResponse;
import vn.cineshow.dto.response.concession.ConcessionTypeResponse;
import vn.cineshow.enums.ConcessionStatus;
import vn.cineshow.enums.StockStatus;
import vn.cineshow.service.ConcessionService;
import vn.cineshow.service.ConcessionTypeService;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.impl.AccountDetailsService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ConcessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConcessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConcessionService concessionService;

    @MockBean
    private ConcessionTypeService concessionTypeService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AccountDetailsService accountDetailsService;

    private ConcessionResponse mockConcession;
    private ConcessionTypeResponse mockConcessionType;

    @BeforeEach
    void setUp() {
        mockConcessionType = ConcessionTypeResponse.builder()
                .id(1L)
                .name("Combo")
                .status("ACTIVE")
                .build();

        mockConcession = ConcessionResponse.builder()
                .concessionId(1L)
                .name("Combo 1")
                .price(50000.0)
                .description("Combo gồm bắp và nước")
                .concessionType(mockConcessionType)
                .unitInStock(100)
                .stockStatus(StockStatus.IN_STOCK)
                .concessionStatus(ConcessionStatus.ACTIVE)
                .urlImage("http://example.com/image.jpg")
                .build();
    }

    // ==================== GET /concession ====================
    @Test
    @DisplayName("GET /concession should return concession list successfully")
    void showListConcessions_shouldReturnConcessionsList() throws Exception {
        List<ConcessionResponse> concessions = Arrays.asList(mockConcession);
        Page<ConcessionResponse> concessionPage = new PageImpl<>(concessions, PageRequest.of(0, 10), 1);

        when(concessionService.getFilteredConcessions(null, null, null, null, 0, 10))
                .thenReturn(concessionPage);

        mockMvc.perform(get("/concession")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Fetched concession list successfully"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].concessionId").value(1L))
                .andExpect(jsonPath("$.data.content[0].name").value("Combo 1"))
                .andExpect(jsonPath("$.data.content[0].price").value(50000.0));

        verify(concessionService, times(1)).getFilteredConcessions(null, null, null, null, 0, 10);
    }

    @Test
    @DisplayName("GET /concession with filters should return filtered concessions")
    void showListConcessions_withFilters_shouldReturnFilteredConcessions() throws Exception {
        List<ConcessionResponse> concessions = Arrays.asList(mockConcession);
        Page<ConcessionResponse> concessionPage = new PageImpl<>(concessions, PageRequest.of(0, 10), 1);

        when(concessionService.getFilteredConcessions("IN_STOCK", 1L, "ACTIVE", "Combo", 0, 10))
                .thenReturn(concessionPage);

        mockMvc.perform(get("/concession")
                        .param("page", "0")
                        .param("size", "10")
                        .param("stockStatus", "IN_STOCK")
                        .param("concessionTypeId", "1")
                        .param("concessionStatus", "ACTIVE")
                        .param("keyword", "Combo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content[0].concessionId").value(1L));

        verify(concessionService, times(1))
                .getFilteredConcessions("IN_STOCK", 1L, "ACTIVE", "Combo", 0, 10);
    }

    @Test
    @DisplayName("GET /concession with empty result should return 404")
    void showListConcessions_emptyResult_shouldReturnNotFound() throws Exception {
        Page<ConcessionResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(concessionService.getFilteredConcessions(null, null, null, null, 0, 10))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/concession")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Không tìm thấy sản phẩm phù hợp."));

        verify(concessionService, times(1)).getFilteredConcessions(null, null, null, null, 0, 10);
    }

    // ==================== POST /concession ====================
    @Test
    @DisplayName("POST /concession should add concession successfully")
    void addConcession_shouldAddSuccessfully() throws Exception {
        Long expectedConcessionId = 1L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        when(concessionService.addConcession(any(ConcessionAddRequest.class)))
                .thenReturn(expectedConcessionId);

        mockMvc.perform(multipart("/concession")
                        .file(file)
                        .param("name", "Combo 1")
                        .param("price", "50000")
                        .param("description", "Combo gồm bắp và nước")
                        .param("concessionTypeId", "1")
                        .param("unitInStock", "100")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Đã thêm sản phẩm thành công"))
                .andExpect(jsonPath("$.data").value(expectedConcessionId));

        ArgumentCaptor<ConcessionAddRequest> captor = ArgumentCaptor.forClass(ConcessionAddRequest.class);
        verify(concessionService, times(1)).addConcession(captor.capture());
        
        ConcessionAddRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.name()).isEqualTo("Combo 1");
        assertThat(capturedRequest.price()).isEqualTo(50000.0);
        assertThat(capturedRequest.concessionTypeId()).isEqualTo(1L);
        assertThat(capturedRequest.unitInStock()).isEqualTo(100);
        assertThat(capturedRequest.file()).isNotNull();
    }

    @Test
    @DisplayName("POST /concession should return 400 when required fields are missing")
    void addConcession_missingFields_shouldReturnBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        // Missing required fields (name, price, etc.)
        mockMvc.perform(multipart("/concession")
                        .file(file)
                        .param("description", "Test description")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(concessionService, never()).addConcession(any());
    }

    @Test
    @DisplayName("POST /concession should return 400 when file is missing")
    void addConcession_missingFile_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(multipart("/concession")
                        .param("name", "Combo 1")
                        .param("price", "50000")
                        .param("concessionTypeId", "1")
                        .param("unitInStock", "100")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(concessionService, never()).addConcession(any());
    }

    // ==================== PUT /concession/{id} ====================
    @Test
    @DisplayName("PUT /concession/{id} should update concession successfully")
    void updateConcession_shouldUpdateSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "updated-image.jpg",
                "image/jpeg",
                "updated image content".getBytes()
        );

        ConcessionResponse updatedResponse = ConcessionResponse.builder()
                .concessionId(1L)
                .name("Updated Combo")
                .price(60000.0)
                .description("Updated description")
                .concessionType(mockConcessionType)
                .unitInStock(150)
                .stockStatus(StockStatus.IN_STOCK)
                .concessionStatus(ConcessionStatus.ACTIVE)
                .urlImage("http://example.com/updated-image.jpg")
                .build();

        when(concessionService.updateConcession(eq(1L), any(ConcessionUpdateRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(multipart("/concession/1")
                        .file(file)
                        .param("name", "Updated Combo")
                        .param("price", "60000")
                        .param("description", "Updated description")
                        .param("concessionTypeId", "1")
                        .param("unitInStock", "150")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Cập nhật sản phẩm thành công"))
                .andExpect(jsonPath("$.data.concessionId").value(1L))
                .andExpect(jsonPath("$.data.name").value("Updated Combo"))
                .andExpect(jsonPath("$.data.price").value(60000.0));

        ArgumentCaptor<ConcessionUpdateRequest> captor = ArgumentCaptor.forClass(ConcessionUpdateRequest.class);
        verify(concessionService, times(1)).updateConcession(eq(1L), captor.capture());
        
        ConcessionUpdateRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.name()).isEqualTo("Updated Combo");
        assertThat(capturedRequest.price()).isEqualTo(60000.0);
    }

    // ==================== PUT /concession/{id}/stock ====================
    @Test
    @DisplayName("PUT /concession/{id}/stock should add stock successfully")
    void addStock_shouldAddStockSuccessfully() throws Exception {
        ConcessionResponse updatedResponse = ConcessionResponse.builder()
                .concessionId(1L)
                .name("Combo 1")
                .price(50000.0)
                .concessionType(mockConcessionType)
                .unitInStock(150) // Updated stock
                .stockStatus(StockStatus.IN_STOCK)
                .concessionStatus(ConcessionStatus.ACTIVE)
                .urlImage("http://example.com/image.jpg")
                .build();

        when(concessionService.addStock(1L, 50)).thenReturn(updatedResponse);

        mockMvc.perform(put("/concession/1/stock")
                        .param("quantityToAdd", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Cập nhật số lượng hàng thành công"))
                .andExpect(jsonPath("$.data.concessionId").value(1L))
                .andExpect(jsonPath("$.data.unitInStock").value(150));

        verify(concessionService, times(1)).addStock(1L, 50);
    }

    @Test
    @DisplayName("PUT /concession/{id}/stock with invalid quantity should return 400")
    void addStock_invalidQuantity_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/concession/1/stock")
                        .param("quantityToAdd", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Số lượng cần thêm phải lớn hơn 0"));

        verify(concessionService, never()).addStock(any(), anyInt());
    }

    @Test
    @DisplayName("PUT /concession/{id}/stock with negative quantity should return 400")
    void addStock_negativeQuantity_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/concession/1/stock")
                        .param("quantityToAdd", "-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Số lượng cần thêm phải lớn hơn 0"));

        verify(concessionService, never()).addStock(any(), anyInt());
    }

    // ==================== PUT /concession/{id}/status ====================
    @Test
    @DisplayName("PUT /concession/{id}/status should update concession status successfully")
    void updateConcessionStatus_shouldUpdateSuccessfully() throws Exception {
        ConcessionResponse updatedResponse = ConcessionResponse.builder()
                .concessionId(1L)
                .name("Combo 1")
                .price(50000.0)
                .concessionType(mockConcessionType)
                .unitInStock(100)
                .stockStatus(StockStatus.IN_STOCK)
                .concessionStatus(ConcessionStatus.INACTIVE) // Updated status
                .urlImage("http://example.com/image.jpg")
                .build();

        when(concessionService.updateConcessionStatus(1L, ConcessionStatus.INACTIVE))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/concession/1/status")
                        .param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Cập nhật trạng thái kinh doanh thành công"))
                .andExpect(jsonPath("$.data.concessionId").value(1L))
                .andExpect(jsonPath("$.data.concessionStatus").value("INACTIVE"));

        verify(concessionService, times(1)).updateConcessionStatus(1L, ConcessionStatus.INACTIVE);
    }

    // ==================== DELETE /concession/{id} ====================
    @Test
    @DisplayName("DELETE /concession/{id} should delete concession successfully")
    void deleteConcession_shouldDeleteSuccessfully() throws Exception {
        doNothing().when(concessionService).deleteConcession(1L);

        mockMvc.perform(delete("/concession/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Đã xóa sản phẩm thành công"));

        verify(concessionService, times(1)).deleteConcession(1L);
    }

    // ==================== GET /concession/types ====================
    @Test
    @DisplayName("GET /concession/types should return all concession types")
    void getAllConcessionTypes_shouldReturnTypesList() throws Exception {
        List<ConcessionTypeResponse> types = Arrays.asList(
                ConcessionTypeResponse.builder()
                        .id(1L)
                        .name("Combo")
                        .status("ACTIVE")
                        .build(),
                ConcessionTypeResponse.builder()
                        .id(2L)
                        .name("Đồ uống")
                        .status("ACTIVE")
                        .build()
        );

        when(concessionTypeService.getAll()).thenReturn(types);

        mockMvc.perform(get("/concession/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Fetched all concession types successfully."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("Combo"))
                .andExpect(jsonPath("$.data[1].id").value(2L))
                .andExpect(jsonPath("$.data[1].name").value("Đồ uống"));

        verify(concessionTypeService, times(1)).getAll();
    }

    // ==================== PUT /concession/types/{id}/status ====================
    @Test
    @DisplayName("PUT /concession/types/{id}/status should update concession type status successfully")
    void updateConcessionTypeStatus_shouldUpdateSuccessfully() throws Exception {
        doNothing().when(concessionTypeService).updateStatus(1L);

        mockMvc.perform(put("/concession/types/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Cập nhật trạng thái loại sản phẩm thành công."));

        verify(concessionTypeService, times(1)).updateStatus(1L);
    }

    // ==================== POST /concession/type ====================
    @Test
    @DisplayName("POST /concession/type should add concession type successfully")
    void addConcessionType_shouldAddSuccessfully() throws Exception {
        ConcessionTypeRequest request = ConcessionTypeRequest.builder()
                .name("Đồ ăn")
                .build();

        doNothing().when(concessionTypeService).addConcessionType("Đồ ăn");

        mockMvc.perform(post("/concession/type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Thêm loại sản phẩm mới thành công."));

        verify(concessionTypeService, times(1)).addConcessionType("Đồ ăn");
    }

    @Test
    @DisplayName("POST /concession/type should return 400 when name is missing")
    void addConcessionType_missingName_shouldReturnBadRequest() throws Exception {
        ConcessionTypeRequest request = ConcessionTypeRequest.builder()
                .name("")
                .build();

        mockMvc.perform(post("/concession/type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(concessionTypeService, never()).addConcessionType(anyString());
    }
}

