package vn.cineshow.service;


import vn.cineshow.dto.request.ticketPrice.TicketPriceRequest;
import vn.cineshow.dto.response.ticketPrice.TicketPriceResponse;
import vn.cineshow.model.TicketPrice;

import java.util.List;

public interface TicketPriceService {

    TicketPrice createOrUpdatePrice(TicketPriceRequest req);
    List<TicketPriceResponse> getAllPrices();
     Double calculatePrice(Long seatId, Long showTimeId);
     TicketPrice findTicketPrice(Long seatId, Long showTimeId);
}
