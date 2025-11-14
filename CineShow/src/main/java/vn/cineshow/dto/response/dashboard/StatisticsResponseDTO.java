package vn.cineshow.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Response DTO cho endpoint thống kê tổng hợp
 * Bao gồm số account, số order và danh sách order với thông tin người tạo
 */
@Data
@Builder
public class StatisticsResponseDTO {
    // Số liệu tổng hợp
    private long totalAccounts;
    private long totalOrders;

    // Danh sách orders với thông tin người tạo
    private List<OrderItemDTO> orders;

    // Thông tin phân trang
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}

