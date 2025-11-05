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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.cineshow.dto.request.holiday.HolidayRequest;
import vn.cineshow.dto.response.holiday.HolidayResponse;
import vn.cineshow.model.Holiday;
import vn.cineshow.service.HolidayService;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.impl.AccountDetailsService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HolidayController.class)
@AutoConfigureMockMvc(addFilters = false)
class HolidayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HolidayService holidayService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AccountDetailsService accountDetailsService;

    // ==================== POST /holidays/create ====================
    @Test
    @DisplayName("POST /holidays/create should create holidays successfully")
    void createHolidays_shouldCreateSuccessfully() throws Exception {
        HolidayRequest request1 = new HolidayRequest("New Year", LocalDate.of(2025, 1, 1), true);
        HolidayRequest request2 = new HolidayRequest("National Day", LocalDate.of(2025, 9, 2), false);
        List<HolidayRequest> requests = Arrays.asList(request1, request2);

        Holiday holiday1 = new Holiday();
        holiday1.setId(1L);
        Holiday holiday2 = new Holiday();
        holiday2.setId(2L);
        List<Holiday> created = Arrays.asList(holiday1, holiday2);

        when(holidayService.addHolidays(any(List.class))).thenReturn(created);

        mockMvc.perform(post("/holidays/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Create holiday list successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[1].id").value(2L));

        verify(holidayService, times(1)).addHolidays(any(List.class));
    }

    // ==================== GET /holidays ====================
    @Test
    @DisplayName("GET /holidays should return holidays list successfully")
    void getHolidays_shouldReturnHolidaysList() throws Exception {
        HolidayResponse response1 = new HolidayResponse(1L, "New Year", "01-01", true);
        HolidayResponse response2 = new HolidayResponse(2L, "National Day", "2025-09-02", false);
        List<HolidayResponse> holidays = Arrays.asList(response1, response2);
        Page<HolidayResponse> page = new PageImpl<>(holidays, PageRequest.of(0, 7), 2);

        when(holidayService.getHolidays("recurring", 1, 7, null)).thenReturn(page);

        mockMvc.perform(get("/holidays")
                        .param("filterType", "recurring")
                        .param("page", "1")
                        .param("limit", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get holiday list successfully"))
                .andExpect(jsonPath("$.data.holidays").isArray())
                .andExpect(jsonPath("$.data.holidays[0].id").value(1L))
                .andExpect(jsonPath("$.data.currentPage").value(1))
                .andExpect(jsonPath("$.data.totalItems").value(2));

        verify(holidayService, times(1)).getHolidays("recurring", 1, 7, null);
    }

    @Test
    @DisplayName("GET /holidays with year parameter should return filtered holidays")
    void getHolidays_withYear_shouldReturnFilteredHolidays() throws Exception {
        HolidayResponse response = new HolidayResponse(1L, "New Year", "2025-01-01", false);
        List<HolidayResponse> holidays = Arrays.asList(response);
        Page<HolidayResponse> page = new PageImpl<>(holidays, PageRequest.of(0, 7), 1);

        when(holidayService.getHolidays("recurring", 1, 7, 2025)).thenReturn(page);

        mockMvc.perform(get("/holidays")
                        .param("filterType", "recurring")
                        .param("page", "1")
                        .param("limit", "7")
                        .param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.holidays[0].id").value(1L));

        verify(holidayService, times(1)).getHolidays("recurring", 1, 7, 2025);
    }

    @Test
    @DisplayName("GET /holidays with default values should use default parameters")
    void getHolidays_withDefaultValues_shouldUseDefaults() throws Exception {
        HolidayResponse response = new HolidayResponse(1L, "New Year", "01-01", true);
        List<HolidayResponse> holidays = Arrays.asList(response);
        Page<HolidayResponse> page = new PageImpl<>(holidays, PageRequest.of(0, 7), 1);

        // Default values: filterType="recurring", page=1, limit=7, year=null
        when(holidayService.getHolidays("recurring", 1, 7, null)).thenReturn(page);

        mockMvc.perform(get("/holidays"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get holiday list successfully"))
                .andExpect(jsonPath("$.data.holidays").isArray())
                .andExpect(jsonPath("$.data.currentPage").value(1))
                .andExpect(jsonPath("$.data.totalPages").exists())
                .andExpect(jsonPath("$.data.totalItems").exists());

        verify(holidayService, times(1)).getHolidays("recurring", 1, 7, null);
    }

    @Test
    @DisplayName("GET /holidays with different filterType should work correctly")
    void getHolidays_withDifferentFilterType_shouldWork() throws Exception {
        HolidayResponse response = new HolidayResponse(1L, "Holiday", "2025-01-01", false);
        List<HolidayResponse> holidays = Arrays.asList(response);
        Page<HolidayResponse> page = new PageImpl<>(holidays, PageRequest.of(0, 10), 1);

        when(holidayService.getHolidays("all", 2, 10, null)).thenReturn(page);

        mockMvc.perform(get("/holidays")
                        .param("filterType", "all")
                        .param("page", "2")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.currentPage").value(2))
                .andExpect(jsonPath("$.data.holidays").isArray());

        verify(holidayService, times(1)).getHolidays("all", 2, 10, null);
    }

    @Test
    @DisplayName("POST /holidays/create with empty list should work")
    void createHolidays_withEmptyList_shouldWork() throws Exception {
        List<HolidayRequest> requests = Arrays.asList();
        List<Holiday> created = Arrays.asList();

        when(holidayService.addHolidays(any(List.class))).thenReturn(created);

        mockMvc.perform(post("/holidays/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Create holiday list successfully"))
                .andExpect(jsonPath("$.data").isArray());

        verify(holidayService, times(1)).addHolidays(any(List.class));
    }

    @Test
    @DisplayName("POST /holidays/create with single holiday should work")
    void createHolidays_withSingleHoliday_shouldWork() throws Exception {
        HolidayRequest request = new HolidayRequest("Single Holiday", LocalDate.of(2025, 5, 1), false);
        List<HolidayRequest> requests = Arrays.asList(request);

        Holiday holiday = new Holiday();
        holiday.setId(1L);
        List<Holiday> created = Arrays.asList(holiday);

        when(holidayService.addHolidays(any(List.class))).thenReturn(created);

        mockMvc.perform(post("/holidays/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L));

        verify(holidayService, times(1)).addHolidays(any(List.class));
    }

    // ==================== DELETE /holidays/{id} ====================
    @Test
    @DisplayName("DELETE /holidays/{id} should delete holiday successfully")
    void deleteHoliday_shouldDeleteSuccessfully() throws Exception {
        doNothing().when(holidayService).deleteHolidayById(1L);

        mockMvc.perform(delete("/holidays/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Delete holiday successfully"));

        verify(holidayService, times(1)).deleteHolidayById(1L);
    }
}

