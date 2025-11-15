package vn.cineshow.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import vn.cineshow.utils.validator.Email;

import java.util.Set;

@Getter
@Setter
public class AccountCreateRequest {

    @NotBlank(message = "Tên không được để trống")
    private String name;

    @Email
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank
    @Size(min = 8, message = "Mật khẩu tối thiểu 8 ký tự")
    private String password;

    @NotNull(message = "Phải chọn ít nhất 1 vai trò")
    @Size(min = 1, message = "Phải chọn ít nhất 1 vai trò")
    private Set<Long> roleIds;   // id các role được chọn
}