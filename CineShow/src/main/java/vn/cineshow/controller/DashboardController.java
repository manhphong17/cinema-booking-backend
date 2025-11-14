package vn.cineshow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.dashboard.AccountListResponseDTO;
import vn.cineshow.dto.response.dashboard.DashboardResponseDTO;
import vn.cineshow.dto.response.dashboard.DashboardSummaryResponse;
import vn.cineshow.dto.response.dashboard.StatisticsResponseDTO;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard Controller")
@Slf4j(topic = "DASHBOARD-CONTROLLER")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
            summary = "Get dashboard summary",
            description = "Get aggregated dashboard statistics from multiple sources in real-time. " +
                    "Includes metrics (total users, new users last 24h, active sessions, logins today), " +
                    "chart data for last N days (registrations and logins), and recent activities. " +
                    "Requires ADMIN or BUSINESS role. " +
                    "Supports partial data if some sources are unavailable."
    )
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseData<DashboardSummaryResponse> getSummary(
            @RequestParam(defaultValue = "7d") String range,
            @RequestParam(defaultValue = "10") int recentSize,
            @RequestParam(defaultValue = "Asia/Ho_Chi_Minh") String tz) {
        log.info("Request to get dashboard summary - range: {}, recentSize: {}, tz: {}", range, recentSize, tz);

        try {
            // Validate range
            if (range == null || range.trim().isEmpty()) {
                log.warn("Invalid range parameter: {}", range);
                throw new AppException(ErrorCode.INVALID_PARAMETER);
            }

            // Validate recentSize
            if (recentSize < 1 || recentSize > 100) {
                log.warn("Invalid recentSize: {}", recentSize);
                throw new AppException(ErrorCode.INVALID_PARAMETER);
            }

            DashboardSummaryResponse summary = dashboardService.getSummary(range, recentSize, tz);

            // Check if all sources failed
            if (summary.isPartial() &&
                    summary.getUnavailableSources() != null &&
                    summary.getUnavailableSources().size() >= 7) {
                log.error("All data sources failed");
                throw new AppException(ErrorCode.INTERNAL_ERROR);
            }

            log.info("Dashboard summary retrieved successfully - partial: {}", summary.isPartial());
            return new ResponseData<>(
                    HttpStatus.OK.value(),
                    summary.isPartial() ? "Dashboard data retrieved with partial results" : "Dashboard data retrieved successfully",
                    summary
            );
        } catch (IllegalArgumentException e) {
            log.warn("Invalid parameter: {}", e.getMessage());
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting dashboard summary", e);
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Operation(
            summary = "Get dashboard data (legacy)",
            description = "Get aggregated dashboard statistics including metrics (total users, new users, logins, active sessions), " +
                    "user activity chart (7 days), and recent activities. " +
                    "Requires ADMIN, OPERATION, or BUSINESS role."
    )
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseData<DashboardResponseDTO> getDashboardData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Request to get dashboard data with filters - startDate: {}, endDate: {}, page: {}, size: {}",
                startDate, endDate, page, size);

        DashboardResponseDTO dashboardData = dashboardService.getDashboardData(startDate, endDate, page, size);

        log.info("Dashboard data retrieved successfully");
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Dashboard data retrieved successfully",
                dashboardData
        );
    }

    @Operation(
            summary = "Get accounts list",
            description = "Get list of accounts with filter by created date and pagination. " +
                    "Also returns total accounts count. " +
                    "Requires ADMIN, OPERATION, or BUSINESS role."
    )
    @GetMapping("/accounts")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseData<AccountListResponseDTO> getAccounts(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Request to get accounts list with filters - startDate: {}, endDate: {}, page: {}, size: {}",
                startDate, endDate, page, size);

        AccountListResponseDTO accountsData = dashboardService.getAccounts(startDate, endDate, page, size);

        log.info("Accounts list retrieved successfully - total: {}, page: {}/{}",
                accountsData.getTotalElements(), page + 1, accountsData.getTotalPages());
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Accounts list retrieved successfully",
                accountsData
        );
    }

    @Operation(
            summary = "Get statistics and details",
            description = "Get all statistics including total accounts, total orders, and order details with creator information. " +
                    "Supports filtering by user ID and date range. " +
                    "Requires ADMIN, OPERATION, or BUSINESS role."
    )
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseData<StatisticsResponseDTO> getStatistics(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Request to get statistics - userId: {}, startDate: {}, endDate: {}, page: {}, size: {}",
                userId, startDate, endDate, page, size);

        // Validate page and size
        if (page < 0) {
            log.warn("Invalid page parameter: {}", page);
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }
        if (size < 1 || size > 100) {
            log.warn("Invalid size parameter: {}", size);
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        StatisticsResponseDTO statistics = dashboardService.getStatistics(userId, startDate, endDate, page, size);

        log.info("Statistics retrieved successfully - totalAccounts: {}, totalOrders: {}, orders in page: {}",
                statistics.getTotalAccounts(), statistics.getTotalOrders(), statistics.getOrders().size());
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Statistics retrieved successfully",
                statistics
        );
    }
}

