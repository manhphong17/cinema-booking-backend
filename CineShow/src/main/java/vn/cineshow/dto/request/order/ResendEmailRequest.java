package vn.cineshow.dto.request.order;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResendEmailRequest {
    @Email(message = "Invalid email format")
    private String toEmail;   // optional
    @Pattern(regexp = "vi|en", message = "Unsupported language")
    private String language;  // optional, default vi
}
