package vn.cineshow.utils.validator;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class QrPolicy {

    // có thể lấy từ config sau này
    public int graceMinutes() { return 30; }

    public boolean isExpired(LocalDateTime showtimeStart, LocalDateTime now) {
        return now.isAfter(showtimeStart.plusMinutes(graceMinutes()));
    }
}
