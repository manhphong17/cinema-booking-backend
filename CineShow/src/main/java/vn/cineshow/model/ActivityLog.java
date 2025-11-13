// trong /model/ActivityLog.java
package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog extends AbstractEntity { // Kế thừa từ AbstractEntity

    // Không cần 'id' và 'createdAt' vì đã có từ AbstractEntity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Đảm bảo bạn có entity 'User'

    @Column(name = "action", nullable = false, length = 50)
    private String action; // Ví dụ: "LOGIN", "CREATE_SHOWTIME"

    @Column(name = "description", length = 500)
    private String description; // Ví dụ: "Đăng nhập thành công"

    // Không cần trường 'timestamp' và @PrePersist
    // Chúng ta sẽ dùng 'createdAt' từ AbstractEntity để thay thế
}