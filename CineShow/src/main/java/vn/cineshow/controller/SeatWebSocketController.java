package vn.cineshow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import vn.cineshow.dto.request.booking.SeatSelectRequest;
import vn.cineshow.service.BookingService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SeatWebSocketController {
    private final BookingService bookingService;

    @MessageMapping("/seat/select")
    @PreAuthorize("hasAnyAuthority('CUSTOMER','STAFF')")
    public void handleSeatSelect(@Valid SeatSelectRequest req) {
        log.info("Received SeatSelectRequest {}", req);
        bookingService.handleSeatAction(req);
    }

}
