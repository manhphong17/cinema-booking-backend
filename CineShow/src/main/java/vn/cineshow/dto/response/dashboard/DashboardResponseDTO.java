// trong /dto/response/DashboardResponseDTO.java
package vn.cineshow.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardResponseDTO {
    private DashboardMetricsDTO metrics;
    private UserActivityChartDTO userActivityChart;
    private List<RecentActivityDTO> recentActivities;
    
    // Pagination info
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}