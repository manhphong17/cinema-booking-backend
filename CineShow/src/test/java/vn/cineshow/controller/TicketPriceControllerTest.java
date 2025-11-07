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
import vn.cineshow.dto.request.ticketPrice.TicketPriceRequest;
import vn.cineshow.dto.response.ticketPrice.TicketPriceResponse;
import vn.cineshow.model.TicketPrice;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.TicketPriceService;
import vn.cineshow.service.impl.AccountDetailsService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TicketPriceController.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketPriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketPriceService ticketPriceService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AccountDetailsService accountDetailsService;

    // ==================== POST /ticket-prices ====================
    @Test
    @DisplayName("POST /ticket-prices should create or update price successfully")
    void createOrUpdatePrice_shouldSaveSuccessfully() throws Exception {
        TicketPriceRequest request = new TicketPriceRequest(1L, 1L, null, 50000.0);

        TicketPrice ticketPrice = new TicketPrice();
        ticketPrice.setId(1L);
        ticketPrice.setPrice(50000.0);

        when(ticketPriceService.createOrUpdatePrice(any(TicketPriceRequest.class))).thenReturn(ticketPrice);

        mockMvc.perform(post("/ticket-prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Ticket price saved successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.price").value(50000.0));

        verify(ticketPriceService, times(1)).createOrUpdatePrice(any(TicketPriceRequest.class));
    }

    // ==================== GET /ticket-prices ====================
    @Test
    @DisplayName("GET /ticket-prices should return all prices successfully")
    void getAllPrices_shouldReturnPricesList() throws Exception {
        TicketPriceResponse response = TicketPriceResponse.builder()
                .roomTypeId(1L)
                .seatTypeId(1L)
                .normalDayPrice(50000.0)
                .build();
        List<TicketPriceResponse> prices = Arrays.asList(response);

        when(ticketPriceService.getAllPrices()).thenReturn(prices);

        mockMvc.perform(get("/ticket-prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Fetched all ticket prices successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].seatTypeId").value(1L))
                .andExpect(jsonPath("$.data[0].normalDayPrice").value(50000.0));

        verify(ticketPriceService, times(1)).getAllPrices();
    }

    // ==================== GET /ticket-prices/calculate ====================
    @Test
    @DisplayName("GET /ticket-prices/calculate should calculate price successfully")
    void getTicketPrice_shouldCalculatePriceSuccessfully() throws Exception {
        Double price = 50000.0;

        when(ticketPriceService.calculatePrice(1L, 10L)).thenReturn(price);

        mockMvc.perform(get("/ticket-prices/calculate")
                        .param("seatId", "1")
                        .param("showTimeId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Lấy giá vé thành công"))
                .andExpect(jsonPath("$.data").value(50000.0));

        verify(ticketPriceService, times(1)).calculatePrice(1L, 10L);
    }
}

