// vn/cineshow/dto/request/EmailRegisterRequest.java
package vn.cineshow.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import vn.cineshow.enums.Gender;

import java.time.LocalDate;

public record EmailRegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 2, max = 100) String name,
        @JsonFormat(pattern = "yyyy-MM-dd") @Past LocalDate dateOfBirth,
        @NotNull Gender gender,
        String address,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Size(min = 8, max = 64) String confirmPassword,
        String otp
) {
}
