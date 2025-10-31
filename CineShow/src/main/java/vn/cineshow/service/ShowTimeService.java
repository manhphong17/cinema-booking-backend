package vn.cineshow.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.request.showtime.CreateShowTimeRequest;
import vn.cineshow.dto.request.showtime.UpdateShowTimeRequest;
import vn.cineshow.dto.response.IdNameDTO;
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
    void softDelete(Long id);
    void restore(Long id); // optional



    List<IdNameDTO> getAllRoomsIdName();

    List<IdNameDTO> getAllSubTitlesIdName();
    List<IdNameDTO> getAllRoomTypesIdName();

    List<IdNameDTO> getIdNameMoviesPlayingAndUpcoming();

}
