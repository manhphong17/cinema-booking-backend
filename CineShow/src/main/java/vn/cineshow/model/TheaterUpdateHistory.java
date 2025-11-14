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

    // Quan hệ Many-to-One với Theater
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    Theater theater;

    // Giữ lại theaterId để backward compatible và cho repository queries
    // Dùng @PostLoad để tự động sync từ theater entity
    @Transient
    Long theaterId;
    
    // Tự động sync theaterId từ theater sau khi load từ DB
    @PostLoad
    private void syncTheaterId() {
        if (theater != null) {
            this.theaterId = theater.getId();
        }
    }
    
    // Getter cho theaterId - backward compatible
    public Long getTheaterId() {
        if (theaterId == null && theater != null) {
            theaterId = theater.getId();
        }
        return theaterId;
    }

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
