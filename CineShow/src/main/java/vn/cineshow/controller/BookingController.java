package vn.cineshow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.booking.ShowTimeResponse;
import vn.cineshow.service.BookingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j(topic = "BOOKING-CONTROLLER")
@Tag(name = "Booking Controller")
public class BookingController {

    BookingService bookingService;

    @Operation(summary = "Get list show times by date and a movie",
            description = "Send a request via this API to get list show times by date and a movie")
    @GetMapping("/movies/{movieId}/show-times/{date}")
    public ResponseData<?> getShowTimesListByDay(@PathVariable Long movieId,
                                                     @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate date){

        List<ShowTimeResponse> showTimesResponses = bookingService.getShowTimesByMovieAndDay(movieId, date);

        return new ResponseData<>(HttpStatus.OK.value(), "Get showtime by movie and day successfully", showTimesResponses);

    }

    @Operation(summary = "Get list show times by start time and a movie",
            description = "Send a request via this API to get list show times by start time and a movie")
    @GetMapping("/movies/{movieId}/show-times/start-time/{startTime}")
    public ResponseData<?> getShowTimesByRoomAndStartTime(@PathVariable Long movieId,
                                                          @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime){

        List<ShowTimeResponse> showTimesResponses = bookingService.getShowTimesByMovieAndStartTime(movieId, startTime);

        return new ResponseData<>(HttpStatus.OK.value(), "Get showtime and room by movie and start time successfully", showTimesResponses);

    }


}
