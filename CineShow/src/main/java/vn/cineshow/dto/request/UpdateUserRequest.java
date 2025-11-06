package vn.cineshow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.cineshow.enums.Gender;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 90000, message = "Avatar URL must not exceed 65535 characters")
    private String avatar;

    private Gender gender;
}
