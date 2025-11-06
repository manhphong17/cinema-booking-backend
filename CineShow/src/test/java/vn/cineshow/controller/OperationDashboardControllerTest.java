package vn.cineshow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.cineshow.dto.response.dashboard.OperationDashboardStatsResponse;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.OperationDashboardService;
import vn.cineshow.service.impl.AccountDetailsService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OperationDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class OperationDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OperationDashboardService operationDashboardService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AccountDetailsService accountDetailsService;

    // ==================== GET /dashboard/operation ====================
    @Test
    @DisplayName("GET /dashboard/operation should return dashboard statistics successfully")
    void getOperationDashboardStats_shouldReturnStatsSuccessfully() throws Exception {
        OperationDashboardStatsResponse stats = OperationDashboardStatsResponse.builder()
                .build();

        when(operationDashboardService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/dashboard/operation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Dashboard statistics retrieved successfully"))
                .andExpect(jsonPath("$.data").exists());

        verify(operationDashboardService, times(1)).getDashboardStats();
    }
}

