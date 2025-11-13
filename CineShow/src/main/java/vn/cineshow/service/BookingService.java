package vn.cineshow.service;

import vn.cineshow.dto.request.booking.SeatSelectRequest;
import vn.cineshow.dto.response.booking.BookingSeatsResponse;
import vn.cineshow.dto.response.booking.ShowTimeResponse;
import vn.cineshow.dto.response.booking.TicketDetailResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import vn.cineshow.dto.response.payment.PaymentMethodDTO;

public interface BookingService {
    List<ShowTimeResponse> getShowTimesByMovieAndDay(Long movieId, LocalDate date);

    List<ShowTimeResponse> getShowTimesByMovieAndStartTime(Long movieId, LocalDateTime startTime);

    List<BookingSeatsResponse> getSeatsByShowTimeId(Long showTimeId);

    void handleSeatAction(SeatSelectRequest req);

    List<TicketDetailResponse> getTicketDetailsByIds(List<Long> ids);
    /**
     * Broadcast BOOKED status to all clients when booking is successful
     * @param showtimeId The showtime ID
     * @param ticketIds List of ticket IDs that were booked
     */
    void broadcastBooked(Long showtimeId, List<Long> ticketIds);

    List<String> getDistinctMethodNames();

    List<String> getDistinctAllMethodNames();

    List<PaymentMethodDTO> getPaymentMethodsByName(String methodName); // mới
}
