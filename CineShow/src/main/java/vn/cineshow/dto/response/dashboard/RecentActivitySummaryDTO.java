package vn.cineshow.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO cho hoạt động gần đây trong summary
 */
@Data
@Builder
public class RecentActivitySummaryDTO {
    private LocalDateTime timestamp;
    private String email; // Email của user
    private String action; // Hành động (LOGIN, CREATE_SHOWTIME, etc.)
    private String description; // Mô tả chi tiết
}

