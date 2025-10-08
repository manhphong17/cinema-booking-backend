package vn.cineshow.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.cineshow.utils.validator.Email;

@Getter
@Setter

public class ForgotPasswordRequest {
    @Email
    @NotBlank
    private String email;
}
