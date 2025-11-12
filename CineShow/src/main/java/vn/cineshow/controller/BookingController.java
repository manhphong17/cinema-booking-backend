package vn.cineshow.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.redis.OrderSessionDTO;

import vn.cineshow.dto.request.booking.ConcessionListRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.booking.BookingSeatsResponse;
import vn.cineshow.dto.response.booking.SeatHold;
import vn.cineshow.dto.response.booking.ShowTimeResponse;
import vn.cineshow.dto.response.booking.TicketDetailResponse;
import vn.cineshow.dto.response.payment.PaymentMethodDTO;
import vn.cineshow.service.BookingService;
import vn.cineshow.service.OrderSessionService;
import vn.cineshow.service.SeatHoldService;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


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

    @Operation(
            summary = "Get current order session (tickets + concessions) from Redis",
            description = "Used by frontend payment page to restore user's selected seats and concessions before payment."
    )
    @GetMapping("/order-session")
    public ResponseData<?> getOrderSession(@RequestParam Long showtimeId,
                                           @RequestParam Long userId) {
        log.info("[ORDER_SESSION][FETCH] Request get order session - showtimeId: {}, userId: {}", showtimeId, userId);

        // 🔹 Gọi service để lấy order session từ Redis
        OrderSessionDTO orderSession = orderSessionService.getOrderSession(showtimeId, userId);

        log.info("[ORDER_SESSION][FETCH] Response order session: {}", orderSession);
        return new ResponseData<>(HttpStatus.OK.value(),
                "Get order session successfully",
                orderSession);
    }

    @GetMapping("/tickets/details")
    public ResponseData<List<TicketDetailResponse>> getTicketDetails(@RequestParam("ids") String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        List<TicketDetailResponse> result = bookingService.getTicketDetailsByIds(idList);
        return new ResponseData<>(HttpStatus.OK.value(), "Get ticket details successfully", result);
    }

    // Lấy danh sách phương thức thanh toán chính
    @GetMapping("/payment-methods/distinct")
    public ResponseData<List<String>> getDistinctMethodNames() {
        List<String> distinctNames = bookingService.getDistinctMethodNames();
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Lấy danh sách nhóm phương thức thanh toán thành công",
                distinctNames
        );
    }

    // Lấy chi tiết các ngân hàng theo methodName
    @GetMapping("/payment-methods/{methodName}")
    public ResponseData<List<PaymentMethodDTO>> getPaymentMethodsByName(
            @PathVariable String methodName) {

        try {
            //  Giải mã UTF-8 path parameter
            String decodedName = URLDecoder.decode(methodName, StandardCharsets.UTF_8);
            log.info("Decoded methodName = {}", decodedName); // test log xem in ra có dấu tiếng Việt chưa


            List<PaymentMethodDTO> methods = bookingService.getPaymentMethodsByName(decodedName);
            return new ResponseData<>(
                    HttpStatus.OK.value(),
                    "Lấy danh sách ngân hàng theo phương thức thành công",
                    methods
            );

        } catch (Exception e) {
            return new ResponseData<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Không thể xử lý tên phương thức thanh toán",
                    null
            );
        }


    }
}
