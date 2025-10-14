package vn.cineshow.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.cineshow.model.PasswordResetToken;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    // Reuse a single row per email (upsert semantics)
    Optional<PasswordResetToken> findByEmail(String email);
}
