// trong /dto/response/UserActivityChartDTO.java
package vn.cineshow.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UserActivityChartDTO {
    private List<DailyStatDTO> logins; // Dữ liệu 7 ngày đăng nhập
    private List<DailyStatDTO> registrations; // Dữ liệu 7 ngày đăng ký
}