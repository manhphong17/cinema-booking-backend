
package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="otp_codes")

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class OtpCode {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String email;

    @Column(nullable=false)
    private String otpHash;

    @Column(nullable=false)
    private Instant expiresAt;

    @Column(nullable=false)
    private boolean used;        // đã verify xong -> true
}
