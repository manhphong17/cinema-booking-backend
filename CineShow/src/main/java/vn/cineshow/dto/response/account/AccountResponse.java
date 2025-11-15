package vn.cineshow.dto.response.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.cineshow.enums.AccountStatus;

import java.time.Instant;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {
    private Long id;
    private String email;
    private AccountStatus status;
    private String name; // <— thêm

    private boolean deleted;

    private Set<RoleItemResponse> roles;

    private Instant createdAt;
}