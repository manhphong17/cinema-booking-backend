package vn.cineshow.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Response DTO cho endpoint /api/dashboard/summary
 * Tổng hợp dữ liệu từ nhiều nguồn để hiển thị dashboard
 */
@Data
@Builder
public class DashboardSummaryResponse {
    // Metrics chính
    private long totalUsers;
    private long newUsersLast24h;
    private Long activeSessions; // Optional
    private long loginsToday;
    
    // Dữ liệu biểu đồ 7 ngày
    private List<LastDayStatDTO> lastDays; // Mảng 7 ngày với đăng ký và đăng nhập
    
    // Hoạt động gần đây
    private List<RecentActivitySummaryDTO> recentActivities;
    
    // Flags cho partial data
    private boolean partial;
    private List<String> unavailableSources; // Danh sách nguồn không khả dụng
}

