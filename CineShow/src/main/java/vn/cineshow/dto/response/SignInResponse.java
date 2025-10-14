package vn.cineshow.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignInResponse {
    private String accessToken;
    private long userId;
    private List<String> roleName;
    private String email;
}
