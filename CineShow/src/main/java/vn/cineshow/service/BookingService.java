package vn.cineshow.service;

import vn.cineshow.dto.response.booking.ShowTimeResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    List<ShowTimeResponse> getShowTimesByMovieAndDay(Long movieId, LocalDate date);

    List<ShowTimeResponse> getShowTimesByMovieAndStartTime(Long movieId, LocalDateTime startTime);
}
