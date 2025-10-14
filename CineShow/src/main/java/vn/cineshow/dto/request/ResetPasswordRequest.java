package vn.cineshow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    // The public token returned by /verify-otp-reset in the format "<tokenId>.<verifier>"
    @NotBlank
    private String resetToken;

    // New password policy: 8–20 chars (adjust if you need stricter rules)
    @NotBlank
    @Size(min = 8, max = 20)
    private String newPassword;
}
