// trong /dto/response/DashboardMetricsDTO.java
package vn.cineshow.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardMetricsDTO {
    private long totalUsers;
    private long newUsersToday;
    private long activeSessions; // Phiên HĐ (ví dụ: hoạt động trong 1h qua)
    private long loginsToday;
}