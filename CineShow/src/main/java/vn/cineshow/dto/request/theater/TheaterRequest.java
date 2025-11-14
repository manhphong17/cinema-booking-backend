package vn.cineshow.dto.request.theater;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class TheaterRequest {
    @NotBlank(message = "Theater name is required")
    @Size(max = 100, message = "Theater name must not exceed 100 characters")
    String name;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    String address;

    @NotBlank(message = "Hotline is required")
    @Size(max = 30, message = "Hotline must not exceed 30 characters")
    String hotline;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid contact email format")
    @Size(max = 120, message = "Contact email must not exceed 120 characters")
    String contactEmail;

    @Size(max = 512, message = "Google Map URL must not exceed 512 characters")
    String googleMapUrl;

    @NotNull(message = "Open time is required")
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime openTime;

    @NotNull(message = "Close time is required")
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime closeTime;

    Boolean overnight;

    @Size(max = 512, message = "Banner URL must not exceed 512 characters")
    String bannerUrl; // cho phép FE gửi lại khi đã upload

    String information; // Thông tin thêm về rạp
}
