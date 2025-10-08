package vn.cineshow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response body for /verify-otp-reset that carries the reset token.
 */
@Data
@AllArgsConstructor
public class VerifyOtpResetResponse {
    private String resetToken;
}
