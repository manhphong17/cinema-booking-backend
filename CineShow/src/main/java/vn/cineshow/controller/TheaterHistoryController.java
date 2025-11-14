package vn.cineshow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.theater.TheaterUpdateHistoryResponse;
import vn.cineshow.service.TheaterUpdateHistoryService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Cung cấp API tra cứu lịch sử cập nhật cấu hình rạp CineShow.
 */
@RestController
@RequestMapping("/api/theater_history")
@RequiredArgsConstructor
public class TheaterHistoryController {

    private final TheaterUpdateHistoryService historyService;

    /**
     * GET /api/theater_history
     * Lấy lịch sử thay đổi của theater với phân trang
     */
    @GetMapping
    public ResponseData<Map<String, Object>> getHistory(
            @RequestParam(defaultValue = "1") Long theaterId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<TheaterUpdateHistoryResponse> historyPage = historyService.getHistory(theaterId, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("history", historyPage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", historyPage.getTotalPages());
        response.put("totalItems", historyPage.getTotalElements());

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Get theater update history successfully",
                response
        );
    }

    /**
     * GET /api/theater_history/by-date-range
     * Lấy lịch sử trong khoảng thời gian
     */
    @GetMapping("/by-date-range")
    public ResponseData<Map<String, Object>> getHistoryByDateRange(
            @RequestParam(defaultValue = "1") Long theaterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<TheaterUpdateHistoryResponse> historyPage = 
                historyService.getHistoryByDateRange(theaterId, startDate, endDate, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("history", historyPage.getContent());
        response.put("currentPage", page);
        response.put("totalPages", historyPage.getTotalPages());
        response.put("totalItems", historyPage.getTotalElements());
        response.put("startDate", startDate);
        response.put("endDate", endDate);

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Get theater update history by date range successfully",
                response
        );
    }
}
