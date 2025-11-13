// trong /service/impl/DashboardServiceImpl.java
package vn.cineshow.service.impl;

// ... imports ...

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.response.dashboard.*;
import vn.cineshow.model.Account;
import vn.cineshow.model.ActivityLog;
import vn.cineshow.model.Role;
import vn.cineshow.model.User;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.ActivityLogRepository;
import vn.cineshow.repository.UserRepository;
import vn.cineshow.service.DashboardService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Lớp triển khai của DashboardService.
 * Chứa logic chính để tính toán và tổng hợp dữ liệu cho Dashboard.
 */
@Service
@RequiredArgsConstructor
@Slf4j(topic = "DASHBOARD-SERVICE")
public class DashboardServiceImpl implements DashboardService {

    // Tiêm (Inject) các repository cần thiết
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final AccountRepository accountRepository;

    // Hằng số cho "Phiên hoạt động" (ví dụ: 1 giờ qua)
    private static final int ACTIVE_SESSION_HOURS = 1;
    // Hằng số cho biểu đồ (7 ngày)
    private static final int CHART_DAYS = 7;
    // Timeout cho mỗi nguồn dữ liệu (giây)
    private static final int SOURCE_TIMEOUT_SECONDS = 3;
    // Timeout tổng (giây)
    private static final int TOTAL_TIMEOUT_SECONDS = 5;

    /**
     * Phương thức chính để tập hợp tất cả dữ liệu dashboard.
     * @param startDate Ngày bắt đầu lọc (format: yyyy-MM-dd), optional
     * @param endDate Ngày kết thúc lọc (format: yyyy-MM-dd), optional
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số lượng items mỗi trang
     * @return DTO chứa tất cả các phần của dashboard.
     */
    @Override
    public DashboardResponseDTO getDashboardData(String startDate, String endDate, int page, int size) {
        // Parse dates
        LocalDateTime startDateTime = parseDate(startDate, true);
        LocalDateTime endDateTime = parseDate(endDate, false);

        // Get paginated activities
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLog> activitiesPage = activityLogRepository.findActivitiesWithFilter(
                startDateTime, endDateTime, pageable);

        return DashboardResponseDTO.builder()
                .metrics(getMetrics(startDateTime, endDateTime))
                .userActivityChart(getUserActivityChart(startDateTime, endDateTime))
                .recentActivities(mapToRecentActivities(activitiesPage.getContent()))
                .currentPage(activitiesPage.getNumber())
                .totalPages(activitiesPage.getTotalPages())
                .totalElements(activitiesPage.getTotalElements())
                .pageSize(activitiesPage.getSize())
                .build();
    }

    /**
     * Parse date string to LocalDateTime.
     * @param dateStr Date string in format yyyy-MM-dd
     * @param isStartOfDay true for start of day (00:00:00), false for end of day (23:59:59)
     * @return LocalDateTime or null if dateStr is null/empty
     */
    private LocalDateTime parseDate(String dateStr, boolean isStartOfDay) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return isStartOfDay ? date.atStartOfDay() : date.atTime(23, 59, 59);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * [Hàm nội bộ] Lấy 4 số liệu thống kê trên cùng.
     * (Tổng user, user mới, phiên HĐ, đăng nhập theo filter).
     * @param startDateTime Thời gian bắt đầu filter (nullable)
     * @param endDateTime Thời gian kết thúc filter (nullable)
     * @return DTO chứa 4 số liệu.
     */
    private DashboardMetricsDTO getMetrics(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Nếu không có filter, dùng hôm nay
        LocalDateTime startOfPeriod = startDateTime != null ? startDateTime : LocalDate.now().atStartOfDay();
        LocalDateTime endOfPeriod = endDateTime != null ? endDateTime : LocalDateTime.now();
        LocalDateTime activeTimeLimit = LocalDateTime.now().minusHours(ACTIVE_SESSION_HOURS);

        // 1. Tổng người dùng
        long totalUsers = userRepository.count();

        // 2. Người dùng mới trong khoảng thời gian
        long newUsersInPeriod = userRepository.countByCreatedAtBetween(startOfPeriod, endOfPeriod);

        // 3. Đăng nhập trong khoảng thời gian
        long loginsInPeriod = activityLogRepository.countByActionAndCreatedAtBetween("LOGIN", startOfPeriod, endOfPeriod);

        // 4. Phiên hoạt động (luôn tính theo thời gian thực)
        long activeSessions = activityLogRepository.countDistinctUsersActiveSince(activeTimeLimit);

        return DashboardMetricsDTO.builder()
                .totalUsers(totalUsers)
                .newUsersToday(newUsersInPeriod)
                .loginsToday(loginsInPeriod)
                .activeSessions(activeSessions)
                .build();
    }

    /**
     * [Hàm nội bộ] Lấy dữ liệu cho biểu đồ đường.
     * (Đăng nhập và Đăng ký trong khoảng thời gian).
     * @param startDateTime Thời gian bắt đầu filter (nullable)
     * @param endDateTime Thời gian kết thúc filter (nullable)
     * @return DTO chứa 2 danh sách dữ liệu cho biểu đồ.
     */
    private UserActivityChartDTO getUserActivityChart(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Nếu không có filter, dùng 7 ngày qua
        LocalDateTime chartStartDate = startDateTime != null ? startDateTime :
                LocalDate.now().minusDays(CHART_DAYS - 1).atStartOfDay();

        List<DailyStatDTO> logins;
        List<DailyStatDTO> registrations;

        if (endDateTime != null) {
            logins = activityLogRepository.getDailyActionStatsWithRange("LOGIN", chartStartDate, endDateTime);
            registrations = userRepository.getDailyRegistrationStatsWithRange(chartStartDate, endDateTime);
        } else {
            logins = activityLogRepository.getDailyActionStats("LOGIN", chartStartDate);
            registrations = userRepository.getDailyRegistrationStats(chartStartDate);
        }

        return UserActivityChartDTO.builder()
                .logins(logins)
                .registrations(registrations)
                .build();
    }

    /**
     * [Hàm nội bộ] Map danh sách ActivityLog thành RecentActivityDTO.
     * @param activityLogs Danh sách ActivityLog
     * @return Danh sách DTO các hoạt động gần đây.
     */
    private List<RecentActivityDTO> mapToRecentActivities(List<ActivityLog> activityLogs) {
        return activityLogs.stream()
                .map(log -> RecentActivityDTO.builder()
                        .timestamp(log.getCreatedAt())
                        .userEmail(log.getUser().getAccount())
                        .action(log.getAction())
                        .description(log.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách account với lọc theo ngày tạo và phân trang.
     * @param startDate Ngày bắt đầu lọc (format: yyyy-MM-dd), optional
     * @param endDate Ngày kết thúc lọc (format: yyyy-MM-dd), optional
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số lượng items mỗi trang
     * @return DTO chứa danh sách account và thông tin phân trang
     */
    @Override
    public AccountListResponseDTO getAccounts(String startDate, String endDate, int page, int size) {
        // Parse dates
        LocalDateTime startDateTime = parseDate(startDate, true);
        LocalDateTime endDateTime = parseDate(endDate, false);

        // Get paginated accounts
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accountsPage = accountRepository.findAccountsWithFilter(
                startDateTime, endDateTime, pageable);

        // Get total accounts count
        long totalAccounts = accountRepository.count();

        // Map to DTO
        List<AccountItemDTO> accountItems = accountsPage.getContent().stream()
                .map(this::mapToAccountItemDTO)
                .collect(Collectors.toList());

        return AccountListResponseDTO.builder()
                .accounts(accountItems)
                .totalAccounts(totalAccounts)
                .currentPage(accountsPage.getNumber())
                .totalPages(accountsPage.getTotalPages())
                .totalElements(accountsPage.getTotalElements())
                .pageSize(accountsPage.getSize())
                .build();
    }

    /**
     * [Hàm nội bộ] Map Account entity sang AccountItemDTO.
     * @param account Account entity
     * @return AccountItemDTO
     */
    private AccountItemDTO mapToAccountItemDTO(Account account) {
        List<String> roleNames = account.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        User user = account.getUser();

        return AccountItemDTO.builder()
                .id(account.getId())
                .email(account.getEmail())
                .status(account.getStatus())
                .roles(roleNames)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .userName(user != null ? user.getName() : null)
                .userGender(user != null && user.getGender() != null ? user.getGender().name() : null)
                .userLoyalPoint(user != null ? user.getLoyalPoint() : null)
                .build();
    }

    /**
     * Lấy dữ liệu tổng hợp dashboard theo thời gian thực từ nhiều nguồn.
     * Orchestrator gọi song song các dịch vụ nguồn và tổng hợp kết quả.
     */
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(String range, int recentSize, String timezone) {
        log.info("Getting dashboard summary - range: {}, recentSize: {}, timezone: {}", range, recentSize, timezone);

        // Parse range (ví dụ: "7d" -> 7 ngày)
        int days = parseRange(range);
        if (days <= 0 || days > 30) {
            throw new IllegalArgumentException("Range must be between 1 and 30 days");
        }

        // Parse timezone
        ZoneId zoneId;
        try {
            zoneId = timezone != null && !timezone.isEmpty()
                    ? ZoneId.of(timezone)
                    : ZoneId.systemDefault();
        } catch (Exception e) {
            log.warn("Invalid timezone: {}, using system default", timezone);
            zoneId = ZoneId.systemDefault();
        }

        // Tính toán thời gian với timezone
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDate today = now.toLocalDate();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);
        LocalDateTime last24hStart = now.minusHours(24).toLocalDateTime();
        LocalDateTime chartStartDate = todayStart.minusDays(days - 1);
        LocalDateTime activeTimeLimit = now.minusHours(ACTIVE_SESSION_HOURS).toLocalDateTime();

        // Danh sách nguồn không khả dụng
        List<String> unavailableSources = Collections.synchronizedList(new ArrayList<>());
        AtomicBoolean partial = new AtomicBoolean(false);

        // Gọi song song các nguồn dữ liệu
        CompletableFuture<Long> totalUsersFuture = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return userRepository.count();
                    } catch (Exception ex) {
                        log.error("Error getting total users: {}", ex.getMessage(), ex);
                        unavailableSources.add("totalUsers");
                        partial.set(true);
                        return 0L;
                    }
                })
                .orTimeout(SOURCE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("Timeout or error getting total users: {}", ex.getMessage(), ex);
                    unavailableSources.add("totalUsers");
                    partial.set(true);
                    return 0L;
                });

        CompletableFuture<Long> newUsersLast24hFuture = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return userRepository.countByCreatedAtBetween(last24hStart, todayEnd);
                    } catch (Exception ex) {
                        log.error("Error getting new users last 24h: {}", ex.getMessage(), ex);
                        unavailableSources.add("newUsersLast24h");
                        partial.set(true);
                        return 0L;
                    }
                })
                .orTimeout(SOURCE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("Timeout or error getting new users last 24h: {}", ex.getMessage(), ex);
                    unavailableSources.add("newUsersLast24h");
                    partial.set(true);
                    return 0L;
                });

        CompletableFuture<Long> activeSessionsFuture = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return activityLogRepository.countDistinctUsersActiveSince(activeTimeLimit);
                    } catch (Exception ex) {
                        log.error("Error getting active sessions: {}", ex.getMessage(), ex);
                        unavailableSources.add("activeSessions");
                        partial.set(true);
                        return null;
                    }
                })
                .orTimeout(SOURCE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("Timeout or error getting active sessions: {}", ex.getMessage(), ex);
                    unavailableSources.add("activeSessions");
                    partial.set(true);
                    return null;
                });

        CompletableFuture<Long> loginsTodayFuture = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return activityLogRepository.countByActionAndCreatedAtBetween("LOGIN", todayStart, todayEnd);
                    } catch (Exception ex) {
                        log.error("Error getting logins today: {}", ex.getMessage(), ex);
                        unavailableSources.add("loginsToday");
                        partial.set(true);
                        return 0L;
                    }
                })
                .orTimeout(SOURCE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("Timeout or error getting logins today: {}", ex.getMessage(), ex);
                    unavailableSources.add("loginsToday");
                    partial.set(true);
                    return 0L;
                });

        CompletableFuture<List<DailyStatDTO>> registrationsFuture = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return userRepository.getDailyRegistrationStatsWithRange(chartStartDate, todayEnd);
                    } catch (Exception ex) {
                        log.error("Error getting daily registrations: {}", ex.getMessage(), ex);
                        unavailableSources.add("dailyRegistrations");
                        partial.set(true);
                        return Collections.<DailyStatDTO>emptyList(); // 👈 sửa
                    }
                })
                .orTimeout(SOURCE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("Timeout or error getting daily registrations: {}", ex.getMessage(), ex);
                    unavailableSources.add("dailyRegistrations");
                    partial.set(true);
                    return Collections.<DailyStatDTO>emptyList();     // 👈 sửa
                });


        CompletableFuture<List<DailyStatDTO>> loginsDailyFuture = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return activityLogRepository.getDailyActionStatsWithRange("LOGIN", chartStartDate, todayEnd);
                    } catch (Exception ex) {
                        log.error("Error getting daily logins: {}", ex.getMessage(), ex);
                        unavailableSources.add("dailyLogins");
                        partial.set(true);
                        return Collections.<DailyStatDTO>emptyList(); // 👈 sửa
                    }
                })
                .orTimeout(SOURCE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("Timeout or error getting daily logins: {}", ex.getMessage(), ex);
                    unavailableSources.add("dailyLogins");
                    partial.set(true);
                    return Collections.<DailyStatDTO>emptyList();     // 👈 sửa
                });


        CompletableFuture<List<ActivityLog>> recentActivitiesFuture = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        PageRequest pageable = PageRequest.of(0, recentSize, Sort.by(Sort.Direction.DESC, "createdAt"));
                        return activityLogRepository.findActivitiesWithFilter(null, null, pageable).getContent();
                    } catch (Exception ex) {
                        log.error("Error getting recent activities", ex);
                        unavailableSources.add("recentActivities");
                        partial.set(true);
                        return new ArrayList<ActivityLog>();
                    }
                })
                .orTimeout(SOURCE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("Error getting recent activities", ex);
                    unavailableSources.add("recentActivities");
                    partial.set(true);
                    return new ArrayList<ActivityLog>();
                });

        // Chờ tất cả hoàn thành với timeout tổng
        try {
            CompletableFuture.allOf(
                    totalUsersFuture, newUsersLast24hFuture, activeSessionsFuture,
                    loginsTodayFuture, registrationsFuture, loginsDailyFuture, recentActivitiesFuture
            ).get(TOTAL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ex) {
            log.error("Timeout or error waiting for all data sources", ex);
            partial.set(true);
        }

        // Lấy kết quả
        long totalUsers = totalUsersFuture.join();
        long newUsersLast24h = newUsersLast24hFuture.join();
        Long activeSessions = activeSessionsFuture.join();
        long loginsToday = loginsTodayFuture.join();
        List<DailyStatDTO> registrations = registrationsFuture.join();
        List<DailyStatDTO> loginsDaily = loginsDailyFuture.join();
        List<ActivityLog> recentActivities = recentActivitiesFuture.join();

        // Tạo map cho dữ liệu biểu đồ
        Map<LocalDate, Long> registrationsMap = registrations.stream()
                .collect(Collectors.toMap(DailyStatDTO::getDate, DailyStatDTO::getCount));
        Map<LocalDate, Long> loginsMap = loginsDaily.stream()
                .collect(Collectors.toMap(DailyStatDTO::getDate, DailyStatDTO::getCount));

        // Tạo mảng lastDays với đầy đủ 7 ngày (điền 0 cho ngày không có dữ liệu)
        List<LastDayStatDTO> lastDays = IntStream.range(0, days)
                .mapToObj(i -> {
                    LocalDate date = today.minusDays(days - 1 - i);
                    return LastDayStatDTO.builder()
                            .date(date)
                            .registrations(registrationsMap.getOrDefault(date, 0L))
                            .logins(loginsMap.getOrDefault(date, 0L))
                            .build();
                })
                .collect(Collectors.toList());

        // Map recent activities
        List<RecentActivitySummaryDTO> recentActivitiesDTO = recentActivities.stream()
                .map(log -> RecentActivitySummaryDTO.builder()
                        .timestamp(log.getCreatedAt())
                        .email(log.getUser() != null && log.getUser().getAccount() != null
                                ? log.getUser().getAccount().getEmail()
                                : "Unknown")
                        .action(log.getAction())
                        .description(log.getDescription())
                        .build())
                .collect(Collectors.toList());

        boolean isPartial = partial.get();
        log.info("Dashboard summary retrieved - partial: {}, unavailableSources: {}", isPartial, unavailableSources);

        return DashboardSummaryResponse.builder()
                .totalUsers(totalUsers)
                .newUsersLast24h(newUsersLast24h)
                .activeSessions(activeSessions)
                .loginsToday(loginsToday)
                .lastDays(lastDays)
                .recentActivities(recentActivitiesDTO)
                .partial(isPartial)
                .unavailableSources(unavailableSources.isEmpty() ? null : unavailableSources)
                .build();
    }

    /**
     * Parse range string (ví dụ: "7d" -> 7)
     */
    private int parseRange(String range) {
        if (range == null || range.trim().isEmpty()) {
            return CHART_DAYS; // Default 7 days
        }
        try {
            String trimmed = range.trim().toLowerCase();
            if (trimmed.endsWith("d")) {
                return Integer.parseInt(trimmed.substring(0, trimmed.length() - 1));
            }
            return Integer.parseInt(trimmed);
        } catch (Exception e) {
            log.warn("Invalid range format: {}, using default {}", range, CHART_DAYS);
            return CHART_DAYS;
        }
    }

}