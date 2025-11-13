// trong /service/DashboardService.java
package vn.cineshow.service;

import vn.cineshow.dto.response.dashboard.DashboardResponseDTO;
import vn.cineshow.dto.response.dashboard.DashboardSummaryResponse;

/**
 * Interface (Giao diện) định nghĩa các dịch vụ (logic nghiệp vụ) 
 * liên quan đến Dashboard.
 * Tách biệt interface và implementation là một practice tốt trong Spring.
 */
public interface DashboardService {

    /**
     * Lấy tất cả dữ liệu tổng hợp cho trang Dashboard.
     * @param startDate Ngày bắt đầu lọc (format: yyyy-MM-dd), optional
     * @param endDate Ngày kết thúc lọc (format: yyyy-MM-dd), optional
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số lượng items mỗi trang
     * @return Một DTO chứa toàn bộ thông tin dashboard.
     */
    DashboardResponseDTO getDashboardData(String startDate, String endDate, int page, int size);

    /**
     * Lấy danh sách account với lọc theo ngày tạo và phân trang.
     * @param startDate Ngày bắt đầu lọc (format: yyyy-MM-dd), optional
     * @param endDate Ngày kết thúc lọc (format: yyyy-MM-dd), optional
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số lượng items mỗi trang
     * @return DTO chứa danh sách account và thông tin phân trang
     */
    vn.cineshow.dto.response.dashboard.AccountListResponseDTO getAccounts(String startDate, String endDate, int page, int size);

    /**
     * Lấy dữ liệu tổng hợp dashboard theo thời gian thực từ nhiều nguồn.
     * Orchestrator gọi song song các dịch vụ nguồn và tổng hợp kết quả.
     * @param range Khoảng thời gian (ví dụ: "7d" cho 7 ngày)
     * @param recentSize Số lượng hoạt động gần đây cần lấy
     * @param timezone Múi giờ (ví dụ: "Asia/Ho_Chi_Minh")
     * @return DashboardSummaryResponse chứa metrics, biểu đồ, và hoạt động gần đây
     */
    DashboardSummaryResponse getSummary(String range, int recentSize, String timezone);
}