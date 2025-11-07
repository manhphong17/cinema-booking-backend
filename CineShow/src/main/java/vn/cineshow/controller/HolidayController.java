package vn.cineshow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.holiday.HolidayRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.holiday.HolidayResponse;
import vn.cineshow.model.Holiday;
import vn.cineshow.service.HolidayService;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseData<List<Holiday>> createHolidays(@RequestBody List<HolidayRequest> requests) {
        List<Holiday> created = holidayService.addHolidays(requests);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Create holiday list successfully",
                created
        );
    }

    /**
     * Lấy danh sách ngày lễ có lọc & phân trang
     * Example:
     * GET /holidays?filterType=recurring&page=1&limit=7
     */
    @GetMapping
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseData<Map<String, Object>> getHolidays(
            @RequestParam(defaultValue = "recurring") String filterType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "7") int limit,
            @RequestParam(required = false) Integer year  // 🆕 thêm param
    ) {
        Page<HolidayResponse> result = holidayService.getHolidays   (filterType, page, limit, year);

        Map<String, Object> res = new HashMap<>();
        res.put("holidays", result.getContent());
        res.put("currentPage", page);
        res.put("totalPages", result.getTotalPages());
        res.put("totalItems", result.getTotalElements());

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Get holiday list successfully",
                res
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseData<Void> deleteHoliday(@PathVariable Long id) {
        holidayService.deleteHolidayById(id);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Delete holiday successfully"
        );
    }
}