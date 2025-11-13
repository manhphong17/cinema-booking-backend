package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.cineshow.dto.response.dashboard.DailyStatDTO;
import vn.cineshow.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAccount_Email(String email);

    /**
     * Đếm số lượng người dùng đăng ký trong một khoảng thời gian cụ thể.
     * @param start Thời điểm bắt đầu (ví dụ: 00:00 hôm nay)
     * @param end Thời điểm kết thúc (ví dụ: 23:59 hôm nay)
     * @return Tổng số lượng người dùng mới
     */
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Lấy thống kê số lượng đăng ký hằng ngày, nhóm theo ngày.
     * Được sử dụng cho dữ liệu biểu đồ "Đăng ký mới".
     * @param startDate Thời điểm bắt đầu lấy thống kê (ví dụ: 7 ngày trước)
     * @return Danh sách DTO chứa (Ngày) và (Số lượng)
     */
    @Query("SELECT new vn.cineshow.dto.response.dashboard.DailyStatDTO(CAST(u.createdAt AS LocalDate), COUNT(u)) " +
            "FROM User u " +
            "WHERE u.createdAt >= :startDate " +
            "GROUP BY CAST(u.createdAt AS LocalDate) " +
            "ORDER BY CAST(u.createdAt AS LocalDate)")
    List<DailyStatDTO> getDailyRegistrationStats(@Param("startDate") LocalDateTime startDate);

    /**
     * Lấy thống kê số lượng đăng ký hằng ngày với filter theo khoảng thời gian.
     * @param startDate Thời điểm bắt đầu
     * @param endDate Thời điểm kết thúc
     * @return Danh sách DTO chứa (Ngày) và (Số lượng)
     */
    @Query("SELECT new vn.cineshow.dto.response.dashboard.DailyStatDTO(CAST(u.createdAt AS LocalDate), COUNT(u)) " +
            "FROM User u " +
            "WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate " +
            "GROUP BY CAST(u.createdAt AS LocalDate) " +
            "ORDER BY CAST(u.createdAt AS LocalDate)")
    List<DailyStatDTO> getDailyRegistrationStatsWithRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}

