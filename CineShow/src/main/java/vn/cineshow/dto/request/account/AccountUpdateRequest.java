package vn.cineshow.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import vn.cineshow.enums.AccountStatus;

import java.util.Set;

@Getter
@Setter
public class AccountUpdateRequest {

    @NotBlank(message = "Tên không được để trống")
    private String name;


    // Trạng thái bắt buộc (ACTIVE / DEACTIVATED / PENDING...)
    @NotNull
    private AccountStatus status;

    // Danh sách id role (nếu null -> BE bỏ qua, nếu [] -> clear hết role)
    private Set<Long> roleIds;

    // Soft delete (true = đánh dấu xóa, false = khôi phục)
    private Boolean deleted;

    // Đổi mật khẩu (optional)
    @Size(min = 8, message = "Mật khẩu tối thiểu 8 ký tự")
    private String newPassword;
}
