package vn.cineshow.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.request.showtime.CreateShowTimeRequest;
import vn.cineshow.dto.request.showtime.UpdateShowTimeRequest;
import vn.cineshow.dto.response.showtime.ShowTimeListDTO;
import vn.cineshow.dto.response.showtime.ShowTimeResponse;
import vn.cineshow.model.ShowTime;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
public interface ShowTimeService {
    Page<ShowTimeListDTO> getAll(Pageable pageable);
    List<ShowTimeListDTO> getAllPlain();                 // không sort/paging
    List<ShowTimeListDTO> getByMovieId(Long movieId, LocalDate date);
    List<ShowTimeListDTO> searchShowtimes(Long movieId, LocalDate date);
    List<ShowTime> searchByRange(String startStr, String endStr, Long roomTypeId, Long movieId);
    List<ShowTime> searchByDate(LocalDate date, Long roomTypeId, Long movieId);
    List<ShowTimeListDTO> getShowtimesByMovieAndDateAndRoomType(Long movieId, LocalDate date, Long roomTypeId);
    List<ShowTimeListDTO> findShowtimes(Long movieId, LocalDate date, Long roomId);
    List<ShowTimeListDTO> findShowtimes(
            Long movieId,
            LocalDate date,
            Long roomId,
            Long roomTypeId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
    ShowTimeResponse createShowTime(CreateShowTimeRequest req);
    ShowTimeListDTO getShowTimeById(Long id);
    ShowTimeResponse updateShowTime(Long id, UpdateShowTimeRequest req);


}
