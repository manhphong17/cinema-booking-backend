package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "theater_update_history")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TheaterUpdateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "theater_id", nullable = false)
    Long theaterId; // luôn = 1

    @Column(name = "updated_field", nullable = false, length = 100)
    String updatedField;

    @Column(name = "old_value", columnDefinition = "TEXT")
    String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    String newValue;

    @Column(name = "updated_by", length = 60)
    String updatedBy;

    @Column(name = "updated_at", nullable = false, updatable = false)
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }
}
