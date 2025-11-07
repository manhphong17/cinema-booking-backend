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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.showtime.CreateShowTimeRequest;
import vn.cineshow.dto.request.showtime.UpdateShowTimeRequest;
import vn.cineshow.dto.response.IdNameDTO;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.showtime.ShowTimeListDTO;
import vn.cineshow.dto.response.showtime.ShowTimeResponse;
import vn.cineshow.service.ShowTimeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowTimeController {

    private final ShowTimeService showTimeService;

    @GetMapping("/lookup/id-name-movies")
    @Operation(summary = "Lookup movies (PLAYING & UPCOMING)",
            description = "Return id-name list of movies whose status is PLAYING or UPCOMING")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseData<List<IdNameDTO>> lookupIdNameMoviesForShowtime() {
        List<IdNameDTO> movies = showTimeService.getIdNameMoviesPlayingAndUpcoming();
        return new ResponseData<>(HttpStatus.OK.value(), "Movies retrieved successfully", movies);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('OPERATION')")
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
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseEntity<List<ShowTimeListDTO>> getAllPlain() {
        return ResponseEntity.ok(showTimeService.getAllPlain());
    }


    @GetMapping("/filter")
    @Operation(summary = "Filter showtimes flexibly",
            description = "Filter showtimes by any combination of parameters (all parameters are optional)")
    @PreAuthorize("hasAuthority('OPERATION')")
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
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseData<List<IdNameDTO>> getAllRoomsIdName() {
        List<IdNameDTO> rooms = showTimeService.getAllRoomsIdName();
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Rooms retrieved successfully",
                rooms
        );
    }


    // LookupController.java (hoặc controller hiện tại của bạn)
    @GetMapping("/subtitles/lookup/id-name")
    @Operation(summary = "Get all subtitles as id-name pairs",
            description = "Retrieve a list of all subtitles with only id and name fields")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseData<List<IdNameDTO>> getAllSubTitlesIdName() {
        List<IdNameDTO> subtitles = showTimeService.getAllSubTitlesIdName();
        return new ResponseData<>(HttpStatus.OK.value(), "Subtitles retrieved successfully", subtitles);
    }

    @GetMapping("/room-types/lookup/id-name")
    @Operation(summary = "Get all room types as id-name pairs",
            description = "Retrieve a list of all room types with only id and name fields")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseData<List<IdNameDTO>> getAllRoomTypesIdName() {
        List<IdNameDTO> roomTypes = showTimeService.getAllRoomTypesIdName();
        return new ResponseData<>(HttpStatus.OK.value(), "Room types retrieved successfully", roomTypes);
    }

    // Luồng: /createShowtime
    @PostMapping("/createShowtime")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseData<ShowTimeResponse> create(@Valid @RequestBody CreateShowTimeRequest req) {
        ShowTimeResponse res = showTimeService.createShowTime(req);
        return new ResponseData<>(HttpStatus.OK.value(), "Create sucess", res);
    }


    @GetMapping("/showtimeBy/{id}")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseEntity<ShowTimeListDTO> getShowtimeById(@PathVariable Long id) {
        return ResponseEntity.ok(showTimeService.getShowTimeById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseEntity<ShowTimeResponse> updatePut(@PathVariable Long id,
                                                      @RequestBody @Valid UpdateShowTimeRequest req) {
        ShowTimeResponse res = showTimeService.updateShowTime(id, req);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseEntity<ShowTimeResponse> updatePatch(@PathVariable Long id,
                                                        @RequestBody UpdateShowTimeRequest req) {
        // Cho phép partial update không @Valid ở tất cả field (service sẽ tự merge & validate)
        ShowTimeResponse res = showTimeService.updateShowTime(id, req);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseData<Void> softDelete(@PathVariable Long id) {
        showTimeService.softDelete(id);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Delete successfully"
        );
    }

    // Tuỳ chọn: khôi phục
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseData<Void> restore(@PathVariable Long id) {
        showTimeService.restore(id);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Restored successfully"
        );
    }

}
