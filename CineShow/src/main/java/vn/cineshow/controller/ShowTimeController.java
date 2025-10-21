package vn.cineshow.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.showtime.CreateShowTimeRequest;
import vn.cineshow.dto.request.showtime.UpdateShowTimeRequest;
import vn.cineshow.dto.response.IdNameDTO;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.showtime.ShowTimeListDTO;
import vn.cineshow.dto.response.showtime.ShowTimeResponse;
import vn.cineshow.repository.MovieRepository;
import vn.cineshow.repository.RoomRepository;
import vn.cineshow.repository.RoomTypeRepository;
import vn.cineshow.repository.SubTitleRepository;
import vn.cineshow.service.ShowTimeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowTimeController {
    // thêm repo để lookup
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final ShowTimeService showTimeService;
    private final MovieRepository movieRepository;
    private final SubTitleRepository subTitleRepository;
    @Operation(summary = "Lookup movies (id + name)")
    @GetMapping("/lookup/id-name-movies")
    public List<IdNameDTO> lookupMovieIdName() {
        return movieRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(m -> new IdNameDTO(m.getId(), m.getName()))
                .toList();
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ShowTimeListDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime,asc") String sort
    ) {
        String[] s = sort.split(",");
        Sort sortObj = (s.length == 2)
                ? Sort.by(Sort.Direction.fromString(s[1]), s[0])
                : Sort.by("startTime").ascending();

        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(showTimeService.getAll(pageable));
    }

    @GetMapping
    public ResponseEntity<List<ShowTimeListDTO>> getAllPlain() {
        return ResponseEntity.ok(showTimeService.getAllPlain());
    }
    @GetMapping("/lookup/room-types")
    public List<IdNameDTO> lookupRoomTypes() {
        return roomTypeRepository.findAll(Sort.by("name").ascending())
                .stream()
                .map(rt -> IdNameDTO.of(rt.getId(), rt.getName()))
                .toList();
    }


    // Dropdown Room (có filter theo roomType)
    @GetMapping("/lookup/rooms")
    public List<IdNameDTO> lookupRooms(@RequestParam(required = false) Long roomTypeId) {
        Sort sort = Sort.by("name").ascending();
        var rooms = (roomTypeId == null)
                ? roomRepository.findAll(sort)
                : roomRepository.findByRoomType_Id(roomTypeId, sort);

        return rooms.stream()
                .map(r -> IdNameDTO.of(r.getId(), r.getName()))
                .toList();
    }

    @GetMapping("/lookup/subtitles")
    public List<IdNameDTO> lookupSubtitles(@RequestParam(required = false) Long subTitleId) {
        Sort sort = Sort.by("name").ascending();
        var subtitles = (subTitleId == null)
                ? subTitleRepository.findAll(sort)
                : subTitleRepository.findSubTitleBy(subTitleId, sort);

        return subtitles.stream()
                .map(s -> IdNameDTO.of(s.getId(), s.getName()))
                .toList();
    }

//    @GetMapping("/showTime")
//    @Operation(summary = "Get showtimes by movie, date and room type",
//            description = "Retrieve showtimes filtered by movie ID, date and room type ID (all parameters are required)")
//    public ResponseData<List<ShowTimeListDTO>> getShowtimes(
//            @RequestParam Long movieId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
//            @RequestParam Long roomTypeId) {
//
//        List<ShowTimeListDTO> showtimes = showTimeService.getShowtimesByMovieAndDateAndRoomType(
//                movieId, date, roomTypeId);
//
//        return new ResponseData<>(
//                HttpStatus.OK.value(),
//                "Showtimes retrieved successfully",
//                showtimes
//        );
//    }

    @GetMapping("/filter")
    @Operation(summary = "Filter showtimes flexibly",
            description = "Filter showtimes by any combination of parameters (all parameters are optional)")
    public ResponseData<List<ShowTimeListDTO>> filterShowtimes(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        List<ShowTimeListDTO> showtimes = showTimeService.findShowtimes(
                movieId,
                date,
                roomId,
                roomTypeId,
                startTime,
                endTime
        );

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Showtimes filtered successfully",
                showtimes
        );
    }

    @GetMapping("/rooms/lookup/id-name")
    @Operation(summary = "Get all rooms as id-name pairs",
            description = "Retrieve a list of all rooms with only id and name fields")
    public ResponseData<List<IdNameDTO>> getAllRoomsIdName() {
        List<IdNameDTO> rooms = roomRepository.findAll().stream()
                .map(room -> IdNameDTO.of(room.getId(), room.getName()))
                .collect(Collectors.toList());

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Rooms retrieved successfully",
                rooms
        );
    }


    @GetMapping("/subtitles/lookup/id-name")
    @Operation(summary = "Get all rooms as id-name pairs",
            description = "Retrieve a list of all rooms with only id and name fields")
    public ResponseData<List<IdNameDTO>> getAllSubTitlesIdName() {
        List<IdNameDTO> subtitles = subTitleRepository.findAll().stream()
                .map(subTitle -> IdNameDTO.of(subTitle.getId(), subTitle.getName()))
                .collect(Collectors.toList());

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Subtitles retrieved successfully",
                subtitles
        );
    }
    @GetMapping("/room-types/lookup/id-name")
    @Operation(summary = "Get all room types as id-name pairs",
            description = "Retrieve a list of all room types with only id and name fields")
    public ResponseData<List<IdNameDTO>> getAllRoomTypesIdName() {
        List<IdNameDTO> roomTypes = roomTypeRepository.findAll().stream()
                .map(roomType -> IdNameDTO.of(roomType.getId(), roomType.getName()))
                .collect(Collectors.toList());

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Room types retrieved successfully",
                roomTypes
        );
    }

    // Luồng: /createShowtime
    @PostMapping("/createShowtime")
    public ResponseEntity<ShowTimeResponse> create(@Valid @RequestBody CreateShowTimeRequest req) {
        ShowTimeResponse res = showTimeService.createShowTime(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/showtimeBy/{id}")
    public ResponseEntity<ShowTimeListDTO> getShowtimeById(@PathVariable Long id) {
        return ResponseEntity.ok(showTimeService.getShowTimeById(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ShowTimeResponse> updatePut(@PathVariable Long id,
                                                      @RequestBody @Valid UpdateShowTimeRequest req) {
        ShowTimeResponse res = showTimeService.updateShowTime(id, req);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ShowTimeResponse> updatePatch(@PathVariable Long id,
                                                        @RequestBody UpdateShowTimeRequest req) {
        // Cho phép partial update không @Valid ở tất cả field (service sẽ tự merge & validate)
        ShowTimeResponse res = showTimeService.updateShowTime(id, req);
        return ResponseEntity.ok(res);
    }


}
