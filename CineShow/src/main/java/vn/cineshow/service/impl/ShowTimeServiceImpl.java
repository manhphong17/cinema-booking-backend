package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.cineshow.dto.request.showtime.CreateShowTimeRequest;
import vn.cineshow.dto.request.showtime.UpdateShowTimeRequest;
import vn.cineshow.dto.response.showtime.ShowTimeListDTO;
import vn.cineshow.dto.response.showtime.ShowTimeResponse;
import vn.cineshow.enums.RoomStatus;
import vn.cineshow.exception.ShowTimeException.AppException;
import vn.cineshow.exception.ShowTimeException.ErrorCodShowTime;
import vn.cineshow.model.Movie;
import vn.cineshow.model.Room;
import vn.cineshow.model.ShowTime;
import vn.cineshow.model.SubTitle;
import vn.cineshow.repository.MovieRepository;
import vn.cineshow.repository.RoomRepository;
import vn.cineshow.repository.ShowTimeRepository;
import vn.cineshow.repository.SubTitleRepository;
import vn.cineshow.service.ShowTimeService;


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

    private final ShowTimeRepository showTimeRepository;
    private final MovieRepository movieRepo;
    private final RoomRepository roomRepo;
    private final SubTitleRepository subTitleRepo;

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
    public List<ShowTimeListDTO> getByMovieId(Long movieId, LocalDate date) {
        List<ShowTime> result;

        if (date == null) {
            result = showTimeRepository.findByMovie_IdOrderByStartTimeAsc(movieId);
        } else {
            LocalDateTime from = date.atStartOfDay();
            LocalDateTime to   = date.plusDays(1).atStartOfDay().minusNanos(1);
            result = showTimeRepository
                    .findByMovie_IdAndStartTimeBetweenOrderByStartTimeAsc(movieId, from, to);
        }

        return result.stream().map(ShowTimeListDTO::from).toList();
    }

    @Override
    public List<ShowTimeListDTO> searchShowtimes(Long movieId, LocalDate date) {
        LocalDateTime from = (date == null) ? null : date.atStartOfDay();
        LocalDateTime to   = (date == null) ? null : date.plusDays(1).atStartOfDay().minusNanos(1);
        List<ShowTime> list = showTimeRepository.search(movieId, from, to);
        return list.stream().map(ShowTimeListDTO::from).toList();
    }

    @Override
    public List<ShowTime> searchByRange(String startStr, String endStr, Long roomTypeId, Long movieId) {
        LocalDateTime start = parseFlexible(startStr, false);
        LocalDateTime end   = parseFlexible(endStr,   true);
        if (start == null || end == null) {
            throw new IllegalArgumentException("start và end là bắt buộc (yyyy-MM-dd[ HH:mm[:ss][.SSSSSS]]).");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("end phải > start.");
        }
        return showTimeRepository.searchOverlap(start, end, roomTypeId, movieId);
    }

    @Override
    public List<ShowTime> searchByDate(LocalDate date, Long roomTypeId, Long movieId) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd   = date.plusDays(1).atStartOfDay();
        return showTimeRepository.searchByDay(dayStart, dayEnd, roomTypeId, movieId);    }

    @Override
    public List<ShowTimeListDTO> getShowtimesByMovieAndDateAndRoomType(Long movieId, LocalDate date, Long roomTypeId) {
        return showTimeRepository.findShowtimes(movieId, date, roomTypeId)
                .stream()
                .map(ShowTimeListDTO::from)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<ShowTimeListDTO> findShowtimes(Long movieId, LocalDate date, Long roomId) {
        return showTimeRepository.findShowtimes(movieId, date, roomId)
                .stream()
                .map(ShowTimeListDTO::from)
                .distinct()
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());    }

    @Override
    @Transactional
    public ShowTimeResponse createShowTime(CreateShowTimeRequest req) {
        // 1) Load entities
        Movie movie = movieRepo.findById(req.getMovieId())
                .orElseThrow(() -> notFound("Movie", req.getMovieId()));
        Room room = roomRepo.findById(req.getRoomId())
                .orElseThrow(() -> notFound("Room", req.getRoomId()));
        SubTitle subtitle = subTitleRepo.findById(req.getSubtitleId())
                .orElseThrow(() -> notFound("Subtitle", req.getSubtitleId()));

        LocalDateTime start = req.getStartTime();
        LocalDateTime end   = req.getEndTime();

        // 2) Validate room status
        if (room.getStatus() == RoomStatus.INACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room is inactive");
        }

        // 3) Validate endTime > startTime + movie.duration
        int durationMinutes = movie.getDuration();
        LocalDateTime minEnd = start.plusMinutes(durationMinutes);
        if (!end.isAfter(minEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "endTime must be greater than startTime + movie.duration (" + durationMinutes + " minutes)");
        }

        // 4) Validate không bị chiếm khung giờ trong cùng phòng (bắt mọi TH overlap)
        //    Điều kiện overlap: (existing.start < end) AND (existing.end > start)
        boolean conflict = showTimeRepository.existsOverlapInRoom(room.getId(), start, end);
        if (conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Showtime conflicts with an existing show in the same room");
        }

        // 5) Persist
        ShowTime st = new ShowTime();
        st.setMovie(movie);
        st.setRoom(room);
        st.setSubtitle(subtitle);
        st.setStartTime(start);
        st.setEndTime(end);

        ShowTime saved = showTimeRepository.save(st);

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
                        new AppException(ErrorCodShowTime.SHOWTIME_NOT_FOUND, "Showtime not found: id=" + id));

        return ShowTimeListDTO.from(st);
    }

    @Override
    public ShowTimeResponse updateShowTime(Long id, UpdateShowTimeRequest req) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "id must be a positive number");
        }

        // Lấy showtime hiện hữu (kèm fetch associations nếu có sẵn)
        ShowTime st = showTimeRepository.findByIdFetchAll(id)
                .orElseThrow(() -> new AppException(ErrorCodShowTime.SHOWTIME_NOT_FOUND, "Showtime not found: id=" + id));

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
        LocalDateTime end   = (req.getEndTime()   != null) ? req.getEndTime()   : st.getEndTime();

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


    // Chấp nhận: "yyyy-MM-dd", "yyyy-MM-ddTHH:mm", "yyyy-MM-dd HH:mm:ss.SSSSSS", v.v.
    private static final DateTimeFormatter FLEX = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart().appendLiteral('T').optionalEnd()
            .optionalStart().appendLiteral(' ').optionalEnd()
            .optionalStart().appendPattern("HH:mm[:ss]").optionalEnd()
            .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 6, true).optionalEnd()
            .toFormatter();

    private static LocalDateTime parseFlexible(String s, boolean endOfDayIfDateOnly) {
        if (s == null || s.isBlank()) return null;
        if (s.trim().length() == "yyyy-MM-dd".length()) {
            LocalDate d = LocalDate.parse(s.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            return endOfDayIfDateOnly ? d.plusDays(1).atStartOfDay() : d.atStartOfDay();
        }
        return LocalDateTime.parse(s.trim(), FLEX);
    }

    private ResponseStatusException notFound(String what, Object id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found: " + id);
    }
}






