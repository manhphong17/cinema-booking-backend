package vn.cineshow.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * DTO cho dữ liệu thống kê theo ngày trong biểu đồ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastDayStatDTO {
    private LocalDate date;
    private long registrations; // Số đăng ký mới
    private long logins; // Số đăng nhập
}

