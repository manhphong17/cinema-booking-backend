package vn.cineshow.dto.response.order;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class ResendEmailResponse {
    private Long orderId;
    private String toEmail;
    private String language;
    private Instant queuedAt;
    private String message;
}
