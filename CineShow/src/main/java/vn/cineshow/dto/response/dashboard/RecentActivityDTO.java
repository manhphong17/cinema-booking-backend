// trong /dto/response/RecentActivityDTO.java
package vn.cineshow.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;
import vn.cineshow.model.Account;

import java.time.LocalDateTime;

@Data
@Builder
public class RecentActivityDTO {
    private LocalDateTime timestamp;
    private Account userEmail; // Hoặc username
    private String action;
    private String description;
}