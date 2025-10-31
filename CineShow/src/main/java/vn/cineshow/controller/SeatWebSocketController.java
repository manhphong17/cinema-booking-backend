package vn.cineshow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import vn.cineshow.dto.request.booking.SeatSelectRequest;
import vn.cineshow.service.SeatHoldService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SeatWebSocketController {
    private final SeatHoldService seatHoldService;

    @MessageMapping("/seat/select")
    public void handleSeatSelect(SeatSelectRequest req) {
        log.info("Received SeatSelectRequest {}", req);
        seatHoldService.processSeatAction(req);
    }

}
