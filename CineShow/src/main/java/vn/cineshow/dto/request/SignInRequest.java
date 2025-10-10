package vn.cineshow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.cineshow.utils.validator.Email;
import vn.cineshow.utils.validator.Password;

import java.io.Serializable;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
public class SignInRequest implements Serializable {

    @Email
    @Schema(example = "user1@gmail.com", defaultValue = "operation@test.con")
    String email;

    @Password
    @Schema(example = "12345678", defaultValue = "12345678")
    String password;

}
