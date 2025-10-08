package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "password_reset_tokens",
        uniqueConstraints = @UniqueConstraint(name = "uk_prt_email", columnNames = "email"),
        indexes = {
                @Index(name = "idx_prt_email_used", columnList = "email, used"),
                @Index(name = "idx_prt_expires_at", columnList = "expires_at")
        }
)

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;  // tokenId used in the public resetToken ("<id>.<verifier>")

    @Column(name = "email", nullable = false, length = 255)
    private String email;  // bound user email (looked up during reset)

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;  // BCrypt(verifier), never store the raw verifier

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;  // short TTL (e.g., 15–30 minutes)

    @Column(name = "used", nullable = false)
    private boolean used;  // one-time consumption flag

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;  // for ordering / auditing

    @Version
    @Column(name = "version")
    private Long version;  // optimistic locking to prevent double-consume

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
