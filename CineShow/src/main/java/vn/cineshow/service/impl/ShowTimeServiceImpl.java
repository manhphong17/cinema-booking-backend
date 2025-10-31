package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.cineshow.dto.request.showtime.CreateShowTimeRequest;
import vn.cineshow.dto.request.showtime.UpdateShowTimeRequest;
import vn.cineshow.dto.response.IdNameDTO;
import vn.cineshow.dto.response.showtime.ShowTimeListDTO;
import vn.cineshow.dto.response.showtime.ShowTimeResponse;
import vn.cineshow.enums.MovieStatus;
import vn.cineshow.enums.RoomStatus;
import vn.cineshow.enums.SeatShowTimeStatus;
import vn.cineshow.enums.SeatStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.*;
import vn.cineshow.repository.*;
import vn.cineshow.service.ShowTimeService;
import vn.cineshow.service.TicketPriceService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowTimeServiceImpl implements ShowTimeService {

    // Chấp nhận: "yyyy-MM-dd", "yyyy-MM-ddTHH:mm", "yyyy-MM-dd HH:mm:ss.SSSSSS", v.v.
    private static final DateTimeFormatter FLEX = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart().appendLiteral('T').optionalEnd()
            .optionalStart().appendLiteral(' ').optionalEnd()
            .optionalStart().appendPattern("HH:mm[:ss]").optionalEnd()
            .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 6, true).optionalEnd()
            .toFormatter();
    private final ShowTimeRepository showTimeRepository;
    private final MovieRepository movieRepo;
    private final RoomRepository roomRepo;
    private final SubTitleRepository subTitleRepo;
    private final RoomTypeRepository roomTypeRepo;
    private final TicketPriceService ticketPriceService;
    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;

    private static LocalDateTime parseFlexible(String s, boolean endOfDayIfDateOnly) {
        if (s == null || s.isBlank()) return null;
        if (s.trim().length() == "yyyy-MM-dd".length()) {
            LocalDate d = LocalDate.parse(s.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            return endOfDayIfDateOnly ? d.plusDays(1).atStartOfDay() : d.atStartOfDay();
        }
        return LocalDateTime.parse(s.trim(), FLEX);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShowTimeListDTO> getAll(Pageable pageable) {
        return showTimeRepository.findAll(pageable)
                .map(ShowTimeListDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowTimeListDTO> getAllPlain() {
        List<ShowTime> list = showTimeRepository.findAllBy();
        return list.stream().map(ShowTimeListDTO::from).collect(Collectors.toList());
    }

    @Override
    public List<ShowTimeListDTO> findShowtimes(Long movieId, LocalDate date, Long roomId, Long roomTypeId, LocalDateTime startTime, LocalDateTime endTime) {
        return showTimeRepository.findShowtimes(
                        movieId,
                        date,
                        roomId,
                        roomTypeId,
                        startTime,
                        endTime
                )
                .stream()
                .map(ShowTimeListDTO::from)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShowTimeResponse createShowTime(CreateShowTimeRequest req) {
        // 1) Load entities
        Movie movie = movieRepo.findById(req.getMovieId())
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        Room room = roomRepo.findById(req.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));
        SubTitle subtitle = subTitleRepo.findById(req.getSubtitleId())
                .orElseThrow(() -> new AppException(ErrorCode.SUBTITLE_NOT_FOUND));

        LocalDateTime start = req.getStartTime();
        LocalDateTime end = req.getEndTime();

        // 2) Validate room status
        if (room.getStatus() == RoomStatus.INACTIVE || room.getStatus() == RoomStatus.MAINTENANCE) {
            throw new AppException(ErrorCode.ROOM_INACTIVE);
        }

        // 3) Validate endTime > startTime + movie.duration
        int durationMinutes = movie.getDuration();
        LocalDateTime minEnd = start.plusMinutes(durationMinutes);
        if (!end.isAfter(minEnd)) {
            throw new AppException(ErrorCode.INVALID_ENDTIME);
        }

        // 4) Validate overlap trong cùng phòng
        //    Overlap nếu: (existing.start < end) AND (existing.end > start)
        boolean conflict = showTimeRepository.existsOverlapInRoom(room.getId(), start, end);
        if (conflict) {
            throw new AppException(ErrorCode.SHOWTIME_CONFLICT);
        }

        // >>> 4.5) Business rule: nếu phim UPCOMING thì chuyển về PLAYING
        if (movie.getStatus() == MovieStatus.UPCOMING) {
            movie.setStatus(MovieStatus.PLAYING);
            // Nếu có audit field:
            // movie.setUpdatedAt(LocalDateTime.now());
            movieRepo.save(movie); // Hibernate sẽ flush trong transaction; gọi save để rõ ràng
        }

        // 5) Persist showtime
        ShowTime st = new ShowTime();
        st.setMovie(movie);
        st.setRoom(room);
        st.setSubtitle(subtitle);
        st.setStartTime(start);
        st.setEndTime(end);
        // nếu có các field khác (price/status/isDeleted...) thì set thêm tại đây
        // st.setStatus(ShowTimeStatus.SCHEDULED);
        // st.setIsDeleted(false);

        ShowTime saved = showTimeRepository.save(st);

        // 6) create seat-showtime
        createSeatForShowTime(saved);

        return ShowTimeResponse.builder()
                .id(saved.getId())
                .movieId(movie.getId())
                .roomId(room.getId())
                .subtitleId(subtitle.getId())
                .startTime(saved.getStartTime())
                .endTime(saved.getEndTime())
                .build();
    }

    @Override
    public ShowTimeListDTO getShowTimeById(Long id) {
        if (id == null || id <= 0) {
            // lỗi validate chung
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "id must be a positive number");
        }

        ShowTime st = showTimeRepository.findByIdFetchAll(id)
                .orElseThrow(() ->
                        new AppException(ErrorCode.SHOW_TIME_NOT_FOUND));

        return ShowTimeListDTO.from(st);
    }

    @Override
    public ShowTimeResponse updateShowTime(Long id, UpdateShowTimeRequest req) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "id must be a positive number");
        }

        // Lấy showtime hiện hữu (kèm fetch associations nếu có sẵn)
        ShowTime st = showTimeRepository.findByIdFetchAll(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHOW_TIME_NOT_FOUND));

        // Merge giá trị mới (nếu null thì giữ nguyên)
        Movie movie = (req.getMovieId() != null)
                ? movieRepo.findById(req.getMovieId()).orElseThrow(() -> notFound("Movie", req.getMovieId()))
                : st.getMovie();

        Room room = (req.getRoomId() != null)
                ? roomRepo.findById(req.getRoomId()).orElseThrow(() -> notFound("Room", req.getRoomId()))
                : st.getRoom();

        SubTitle subtitle = (req.getSubtitleId() != null)
                ? subTitleRepo.findById(req.getSubtitleId()).orElseThrow(() -> notFound("Subtitle", req.getSubtitleId()))
                : st.getSubtitle();

        LocalDateTime start = (req.getStartTime() != null) ? req.getStartTime() : st.getStartTime();
        LocalDateTime end = (req.getEndTime() != null) ? req.getEndTime() : st.getEndTime();

        // Validate đủ cặp thời gian
        if (start == null || end == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime and endTime are required");
        }

        // 1) Validate room status
        if (room.getStatus() == RoomStatus.INACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room is inactive");
        }

        // 2) Validate endTime > startTime + movie.duration
        int durationMinutes = movie.getDuration();
        LocalDateTime minEnd = start.plusMinutes(durationMinutes);
        if (!end.isAfter(minEnd)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "endTime must be greater than startTime + movie.duration (" + durationMinutes + " minutes)"
            );
        }

        // 3) Validate không overlap trong cùng phòng, loại trừ chính showtime đang update
        boolean conflict = showTimeRepository.existsOverlapInRoomExcludingId(room.getId(), start, end, id);
        if (conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Showtime conflicts with an existing show in the same room");
        }

        // 4) Gán và lưu
        st.setMovie(movie);
        st.setRoom(room);
        st.setSubtitle(subtitle);
        st.setStartTime(start);
        st.setEndTime(end);

        ShowTime saved = showTimeRepository.save(st);

        return ShowTimeResponse.builder()
                .id(saved.getId())
                .movieId(saved.getMovie().getId())
                .roomId(saved.getRoom().getId())
                .subtitleId(saved.getSubtitle().getId())
                .startTime(saved.getStartTime())
                .endTime(saved.getEndTime())
                .build();
    }

    @Transactional
    @Override
    public void softDelete(Long id) {
        ShowTime st = showTimeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHOW_TIME_NOT_FOUND));
        st.setIsDeleted(true);
        showTimeRepository.save(st);
        // có thể đã xoá mềm trước đó hoặc race condition

    }

    @Override
    public void restore(Long id) {
        int n = showTimeRepository.restore(id);
        if (n == 0) {
            throw new AppException(ErrorCode.SHOW_TIME_NOT_FOUND);
        }
    }

    @Override
    public List<IdNameDTO> lookupMovieIdNameForUpcoming(LocalDateTime from) {
        // Lấy danh sách movieId có suất chiếu chưa bắt đầu và chưa xoá mềm
        List<Long> movieIds = showTimeRepository.findDistinctMovieIdsForUpcoming(from);
        if (movieIds.isEmpty()) return List.of();

        return movieRepo.findAllById(movieIds).stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(m -> new IdNameDTO(m.getId(), m.getName()))
                .toList();
    }

    @Override
    public List<IdNameDTO> lookupMovieIdNameByStatuses() {
        return movieRepo
                .findByStatusIn(List.of(MovieStatus.PLAYING, MovieStatus.UPCOMING),
                        Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(m -> IdNameDTO.of(m.getId(), m.getName()))
                .toList();
    }

    @Override
    public List<IdNameDTO> getAllRoomsIdName() {
        return roomRepo.findAll().stream()
                .map(room -> IdNameDTO.of(room.getId(), room.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<IdNameDTO> getAllSubTitlesIdName() {
        return subTitleRepo.findAll().stream()
                .map(st -> IdNameDTO.of(st.getId(), st.getName()))
                .collect(Collectors.toList());
    }

//    @Override
//    public Page<ShowTimeListDTO> findShowtimesPaged(Long movieId, LocalDate date, Long roomId,
//                                                    Long roomTypeId, LocalDateTime startTime, LocalDateTime endTime,
//                                                    int page, int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
//
//        return showTimeRepository.findShowtimesPaged(
//                movieId, date, roomId, roomTypeId, startTime, endTime, pageable
//        ).map(ShowTimeListDTO::from);
//    }

    @Override
    public List<IdNameDTO> getAllRoomTypesIdName() {
        return roomTypeRepo.findAll().stream()
                .map(rt -> IdNameDTO.of(rt.getId(), rt.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<IdNameDTO> getIdNameMoviesPlayingAndUpcoming() {
        return movieRepo.findAllIdNameByStatuses(
                List.of(MovieStatus.PLAYING, MovieStatus.UPCOMING)
        );
    }

    private ResponseStatusException notFound(String what, Object id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found: " + id);
    }


    /**
     * Initializes all seats for a specific showtime.
     * <p>
     * This method retrieves all seats belonging to the showtime's room,
     * calculates the ticket price for each seat based on current pricing policy,
     * determines the seat status (AVAILABLE or BLOCKED),
     * and persists all generated SeatShowTime records into the database.
     * </p>
     *
     * @param showTime the showtime for which seat entries should be created
     */
    private void createSeatForShowTime(ShowTime showTime) {
        //add seat
        List<Seat> seats = seatRepository.findByRoom(showTime.getRoom());

        for (Seat seat : seats) {
            TicketPrice ticketPrice = ticketPriceService.findTicketPrice(seat.getId(), showTime.getId());
            Ticket ticket = Ticket.builder()
                    .seat(seat)
                    .ticketPrice(ticketPrice)
                    .showTime(showTime)
                    .build();

            ticket.setStatus(
                    seat.getStatus() == SeatStatus.AVAILABLE ? SeatShowTimeStatus.AVAILABLE : SeatShowTimeStatus.BLOCKED
            );
            //save
            ticketRepository.save(ticket);
        }
    }

}






