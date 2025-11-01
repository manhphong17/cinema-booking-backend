package vn.cineshow.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.cineshow.dto.response.booking.BookingSeatsResponse;
import vn.cineshow.dto.response.booking.ShowTimeResponse;
import vn.cineshow.service.BookingService;
import vn.cineshow.service.SeatHoldService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private SeatHoldService seatHoldService;

    @Test
    @DisplayName("GET /bookings/movies/{movieId}/show-times/{date} should return showtimes by movie and date")
    void getShowTimesByMovieAndDay_shouldReturnShowTimes() throws Exception {
        ShowTimeResponse showTime = ShowTimeResponse.builder()
                .showTimeId(1L)
                .startTime(LocalDateTime.of(2024, 12, 25, 14, 0))
                .endTime(LocalDateTime.of(2024, 12, 25, 16, 30))
                .roomId(1L)
                .roomName("Phòng 1")
                .roomType("2D")
                .totalSeat(100L)
                .totalSeatAvailable(85L)
                .build();

        List<ShowTimeResponse> showTimes = Arrays.asList(showTime);

        when(bookingService.getShowTimesByMovieAndDay(1L, LocalDate.of(2024, 12, 25)))
                .thenReturn(showTimes);

        mockMvc.perform(get("/bookings/movies/1/show-times/2024-12-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get showtime by movie and day successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].showTimeId").value(1L))
                .andExpect(jsonPath("$.data[0].roomName").value("Phòng 1"));

        verify(bookingService).getShowTimesByMovieAndDay(1L, LocalDate.of(2024, 12, 25));
    }

    @Test
    @DisplayName("GET /bookings/movies/{movieId}/show-times/start-time/{startTime} should return showtimes by start time")
    void getShowTimesByStartTime_shouldReturnShowTimes() throws Exception {
        ShowTimeResponse showTime = ShowTimeResponse.builder()
                .showTimeId(1L)
                .startTime(LocalDateTime.of(2024, 12, 25, 14, 0))
                .endTime(LocalDateTime.of(2024, 12, 25, 16, 30))
                .roomId(1L)
                .roomName("Phòng 1")
                .roomType("2D")
                .totalSeat(100L)
                .totalSeatAvailable(85L)
                .build();

        List<ShowTimeResponse> showTimes = Arrays.asList(showTime);
        LocalDateTime startTime = LocalDateTime.of(2024, 12, 25, 14, 0);

        when(bookingService.getShowTimesByMovieAndStartTime(1L, startTime))
                .thenReturn(showTimes);

        mockMvc.perform(get("/bookings/movies/1/show-times/start-time/2024-12-25T14:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get showtime and room by movie and start time successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].showTimeId").value(1L));

        verify(bookingService).getShowTimesByMovieAndStartTime(1L, startTime);
    }

    @Test
    @DisplayName("GET /bookings/show-times/{showTimeId}/seats should return seats for booking")
    void getSeatsForBooking_shouldReturnSeats() throws Exception {
        BookingSeatsResponse seatsResponse = BookingSeatsResponse.builder()
                .showTimeId(1L)
                .roomId(1L)
                .ticketResponses(Collections.emptyList())
                .build();

        List<BookingSeatsResponse> seatsResponses = Arrays.asList(seatsResponse);

        when(bookingService.getSeatsByShowTimeId(1L)).thenReturn(seatsResponses);

        mockMvc.perform(get("/bookings/show-times/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get showtime and room by movie and start time successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].showTimeId").value(1L))
                .andExpect(jsonPath("$.data[0].roomId").value(1L));

        verify(bookingService).getSeatsByShowTimeId(1L);
    }

    @Test
    @DisplayName("GET /bookings/show-times/{showtimeId}/users/{userId}/seat-hold/ttl should return remaining TTL")
    void getSeatHoldTTL_shouldReturnRemainingTime() throws Exception {
        when(seatHoldService.getExpire(1L, 1L)).thenReturn(300L);

        mockMvc.perform(get("/bookings/show-times/1/users/1/seat-hold/ttl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get seat hold TTL successfully"))
                .andExpect(jsonPath("$.data").value(300L));

        verify(seatHoldService).getExpire(1L, 1L);
    }
}

