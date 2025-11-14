// trong /repository/ActivityLogRepository.java
package vn.cineshow.repository;

// ... imports ...

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.cineshow.dto.response.dashboard.DailyStatDTO;
import vn.cineshow.model.ActivityLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository quản lý việc truy cập dữ liệu cho entity ActivityLog.
 * Dùng để truy vấn lịch sử hoạt động của người dùng.
 */
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * Đếm số lượng log theo một hành động cụ thể (ví dụ: "LOGIN") 
     * trong một khoảng thời gian.
     * @param action Tên hành động (ví dụ: "LOGIN")
     * @param start Thời điểm bắt đầu
     * @param end Thời điểm kết thúc
     * @return Số lượng log
     */
    long countByActionAndCreatedAtBetween(String action, LocalDateTime start, LocalDateTime end);

    /**
     * Đếm số lượng người dùng (duy nhất) có hoạt động kể từ một thời điểm.
     * Dùng để tính "Phiên hoạt động" (ví dụ: số user hoạt động trong 1 giờ qua).
     * @param since Thời điểm mốc (ví dụ: 1 giờ trước)
     * @return Số lượng người dùng đang hoạt động
     */
    @Query("SELECT COUNT(DISTINCT a.user.id) FROM ActivityLog a WHERE a.createdAt >= :since")
    long countDistinctUsersActiveSince(@Param("since") LocalDateTime since);

    /**
     * Lấy 10 hoạt động mới nhất, sắp xếp theo thời gian giảm dần (dựa trên createdAt).
     * @return Danh sách 10 log mới nhất
     */
    List<ActivityLog> findTop10ByOrderByCreatedAtDesc();

    /**
     * Lấy hoạt động với phân trang và lọc theo khoảng thời gian.
     * Sử dụng EntityGraph để eager fetch User và Account để tránh LazyInitializationException.
     * @param startDate Thời điểm bắt đầu (nullable)
     * @param endDate Thời điểm kết thúc (nullable)
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách ActivityLog
     */
    @EntityGraph(attributePaths = {"user", "user.account"})
    @Query("SELECT a FROM ActivityLog a WHERE " +
            "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR a.createdAt <= :endDate) " +
            "ORDER BY a.createdAt DESC")
    Page<ActivityLog> findActivitiesWithFilter(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Lấy thống kê số lượng hành động hằng ngày (ví dụ: "LOGIN"), nhóm theo ngày.
     * Dùng cho biểu đồ "Đăng nhập".
     * @param action Tên hành động cần thống kê
     * @param startDate Thời điểm bắt đầu lấy thống kê
     * @return Danh sách DTO chứa (Ngày) và (Số lượng)
     */
    @Query("SELECT new vn.cineshow.dto.response.dashboard.DailyStatDTO(CAST(a.createdAt AS LocalDate), COUNT(a)) " +
            "FROM ActivityLog a " +
            "WHERE a.action = :action AND a.createdAt >= :startDate " +
            "GROUP BY CAST(a.createdAt AS LocalDate) " +
            "ORDER BY CAST(a.createdAt AS LocalDate)")
    List<DailyStatDTO> getDailyActionStats(@Param("action") String action, @Param("startDate") LocalDateTime startDate);

    /**
     * Lấy thống kê số lượng hành động hằng ngày với filter theo khoảng thời gian.
     * @param action Tên hành động cần thống kê
     * @param startDate Thời điểm bắt đầu
     * @param endDate Thời điểm kết thúc
     * @return Danh sách DTO chứa (Ngày) và (Số lượng)
     */
    @Query("SELECT new vn.cineshow.dto.response.dashboard.DailyStatDTO(CAST(a.createdAt AS LocalDate), COUNT(a)) " +
            "FROM ActivityLog a " +
            "WHERE a.action = :action AND a.createdAt >= :startDate AND a.createdAt <= :endDate " +
            "GROUP BY CAST(a.createdAt AS LocalDate) " +
            "ORDER BY CAST(a.createdAt AS LocalDate)")
    List<DailyStatDTO> getDailyActionStatsWithRange(
            @Param("action") String action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}