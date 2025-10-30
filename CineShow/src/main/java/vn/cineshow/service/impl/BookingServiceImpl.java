package vn.cineshow.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.response.booking.BookingSeatsResponse;
import vn.cineshow.dto.response.booking.ShowTimeResponse;
import vn.cineshow.dto.response.booking.TicketResponse;
import vn.cineshow.enums.SeatStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.Movie;
import vn.cineshow.model.Room;
import vn.cineshow.model.ShowTime;
import vn.cineshow.repository.MovieRepository;
import vn.cineshow.repository.ShowTimeRepository;
import vn.cineshow.service.BookingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


@Service
@Slf4j(topic = "BOOKING-SERVICE")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl implements BookingService {
    MovieRepository movieRepository;
    ShowTimeRepository showTimeRepository;

    @Override
    public List<ShowTimeResponse> getShowTimesByMovieAndDay(Long movieId, LocalDate date) {

        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        List<Object[]> showTimes = showTimeRepository.findDistinctStartAndEndTimesByDate(date, movie.getId());

        return showTimes.stream()
                .map(s -> ShowTimeResponse.builder()
                        .startTime((LocalDateTime) s[0])
                        .endTime((LocalDateTime) s[1])
                        .build())
                .toList();
    }

    @Override
    public List<ShowTimeResponse> getShowTimesByMovieAndStartTime(Long movieId, LocalDateTime startTime) {
        return showTimeRepository.findByMovie_IdAndStartTime(movieId, startTime).stream()
                .filter(showTime -> countTotalSeatAvailable(showTime.getRoom()) > 0)
                .map(s -> ShowTimeResponse.builder()
                        .showTimeId(s.getId())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .roomId(s.getRoom().getId())
                        .roomType(s.getRoom().getRoomType().getName())
                        .roomName(s.getRoom().getName())
                        .totalSeat(s.getRoom().getSeats().size())
                        .totalSeatAvailable(countTotalSeatAvailable(s.getRoom()))
                        .build())
                .toList();
    }

    @Override
    public List<BookingSeatsResponse> getSeatsByShowTimeId(Long showTimeId) {
        ShowTime showTime = showTimeRepository.findById(showTimeId).orElseThrow(()
                -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));

        return Collections.singletonList(BookingSeatsResponse.builder()
                .showTimeId(showTime.getId())
                .roomId(showTime.getRoom().getId())
                .ticketResponses(showTime.getTickets().stream()
                        .map(ticket -> TicketResponse.builder()
                                .seatId(ticket.getSeat().getId())
                                .ticketId(ticket.getId())
                                .seatStatus(ticket.getStatus().name())
                                .seatType(ticket.getSeat().getSeatType().getName())
                                .rowIdx(Integer.parseInt(ticket.getSeat().getRow()) - 1)
                                .columnInx(Integer.parseInt(ticket.getSeat().getColumn()) - 1)
                                .ticketPrice(ticket.getTicketPrice().getPrice())
                                .build())
                        .toList())
                .build());

    }


    private int countTotalSeatAvailable(Room room) {
        return room.getSeats().stream().filter(seat -> seat.getStatus().equals(SeatStatus.AVAILABLE)).toList().size();
    }
}
