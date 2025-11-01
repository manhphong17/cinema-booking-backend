package vn.cineshow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.booking.ConcessionListRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.booking.BookingSeatsResponse;
import vn.cineshow.dto.response.booking.SeatHold;
import vn.cineshow.dto.response.booking.ShowTimeResponse;
import vn.cineshow.service.BookingService;
import vn.cineshow.service.OrderSessionService;
import vn.cineshow.service.SeatHoldService;

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
    SeatHoldService seatHoldService;
    OrderSessionService orderSessionService;

    @Operation(summary = "Get list show times by date and a movie",
            description = "Send a request via this API to get list show times by date and a movie")
    @GetMapping("/movies/{movieId}/show-times/{date}")
    public ResponseData<?> getShowTimesListByDay(@PathVariable Long movieId,
                                                 @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        List<ShowTimeResponse> showTimesResponses = bookingService.getShowTimesByMovieAndDay(movieId, date);

        return new ResponseData<>(HttpStatus.OK.value(), "Get showtime by movie and day successfully", showTimesResponses);

    }

    @Operation(summary = "Get list show times by start time and a movie",
            description = "Send a request via this API to get list show times by start time and a movie")
    @GetMapping("/movies/{movieId}/show-times/start-time/{startTime}")
    public ResponseData<?> getShowTimesByRoomAndStartTime(@PathVariable Long movieId,
                                                          @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime) {

        List<ShowTimeResponse> showTimesResponses = bookingService.getShowTimesByMovieAndStartTime(movieId, startTime);

        return new ResponseData<>(HttpStatus.OK.value(), "Get showtime and room by movie and start time successfully", showTimesResponses);

    }

    @Operation(summary = "Get seats for booking by showtimeId",
            description = "Send a request via this API to get list seat for booking by showtimeId")
    @GetMapping("/show-times/{showTimeId}/seats")
    public ResponseData<?> getSeatsForBooking(@PathVariable Long showTimeId) {

        log.info("Request get seats for booking by showtimeId: {}", showTimeId);
        List<BookingSeatsResponse> seatResponses = bookingService.getSeatsByShowTimeId(showTimeId);
        log.info("Response get seats for booking by showtimeId: {}", seatResponses);
        return new ResponseData<>(HttpStatus.OK.value(), "Get showtime and room by movie and start time successfully", seatResponses);
    }

    @Operation(
            summary = "Get remaining TTL of user's seat hold",
            description = "Return remaining time (in seconds) before user's seat hold expires in Redis. " +
                    "Used by frontend to display countdown when page reloads."
    )
    @GetMapping("/show-times/{showtimeId}/users/{userId}/seat-hold/ttl")
    public ResponseData<?> getSeatHoldTTL(@PathVariable Long showtimeId,
                                          @PathVariable Long userId) {
        long ttl = seatHoldService.getExpire(showtimeId, userId);
        return new ResponseData<>(HttpStatus.OK.value(), "Get seat hold TTL successfully", ttl);
    }

    @Operation(
            summary = "Get current seat hold for a user",
            description = "Return the current seat hold information for a user in a specific showtime. " +
                    "Used by frontend to restore held seats when page reloads."
    )
    @GetMapping("/show-times/{showtimeId}/users/{userId}/seat-hold")
    public ResponseData<?> getCurrentSeatHold(@PathVariable Long showtimeId,
                                               @PathVariable Long userId) {
        log.info("Request get current seat hold - showtimeId: {}, userId: {}", showtimeId, userId);
        SeatHold seatHold = seatHoldService.getCurrentHold(showtimeId, userId);
        log.info("Response get current seat hold: {}", seatHold);
        return new ResponseData<>(HttpStatus.OK.value(), "Get current seat hold successfully", seatHold);
    }

    @Operation(
            summary = "Add concession (combo) to current order session",
            description = "Store selected concession items in Redis together with seat hold. " +
                    "TTL will match order session to ensure synchronization."
    )
    @PostMapping("/order-session/concessions")
    public ResponseData<?> addConcessionListToOrderSession(@RequestBody @Valid ConcessionListRequest request) {
        orderSessionService.addOrUpdateCombos(request);
        return new ResponseData<>(HttpStatus.OK.value(), "Add concessions to order session successfully");
    }

}
