package vn.cineshow.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;
import vn.cineshow.enums.AccountStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AccountItemDTO {
    private Long id;
    private String email;
    private AccountStatus status;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // User info
    private String userName;
    private String userGender;
    private Integer userLoyalPoint;
}
