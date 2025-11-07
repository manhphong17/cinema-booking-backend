package vn.cineshow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.dashboard.OperationDashboardStatsResponse;
import vn.cineshow.service.OperationDashboardService;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard Controller")
@Slf4j(topic = "OPERATION-DASHBOARD-CONTROLLER")
public class OperationDashboardController {

    private final OperationDashboardService operationDashboardService;

    @Operation(
            summary = "Get operation dashboard statistics",
            description = "Send a request via this API to get operation dashboard statistics including movies, showtimes, rooms stats, alerts and insights"
    )
    @GetMapping("/operation")
    @PreAuthorize("hasAuthority('OPERATION')")
    public ResponseData<OperationDashboardStatsResponse> getOperationDashboardStats() {
        log.info("Request get operation dashboard statistics");

        OperationDashboardStatsResponse stats = operationDashboardService.getDashboardStats();

        log.info("Response get operation dashboard statistics successfully");
        return new ResponseData<>(HttpStatus.OK.value(), "Dashboard statistics retrieved successfully", stats);
    }
}

