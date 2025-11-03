package vn.cineshow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.ticketPrice.TicketPriceRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.ticketPrice.TicketPriceResponse;
import vn.cineshow.model.TicketPrice;
import vn.cineshow.service.TicketPriceService;

import java.util.List;

@RestController
@RequestMapping("/ticket-prices")
@RequiredArgsConstructor
public class TicketPriceController {

    private final TicketPriceService ticketPriceService;

    @PostMapping
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseData<TicketPrice> createOrUpdatePrice(@RequestBody TicketPriceRequest req) {
        TicketPrice updated = ticketPriceService.createOrUpdatePrice(req);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Ticket price saved successfully",
                updated
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseData<List<TicketPriceResponse>> getAllPrices() {
        List<TicketPriceResponse> list = ticketPriceService.getAllPrices();
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Fetched all ticket prices successfully",
                list
        );
    }

    // GET /ticket-prices/calculate?seatId=1&showTimeId=10
    @GetMapping("/calculate")
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseData<Double> getTicketPrice(
            @RequestParam Long seatId,
            @RequestParam Long showTimeId
    ) {
        Double price = ticketPriceService.calculatePrice(seatId, showTimeId);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Lấy giá vé thành công",
                price
        );
    }
}
