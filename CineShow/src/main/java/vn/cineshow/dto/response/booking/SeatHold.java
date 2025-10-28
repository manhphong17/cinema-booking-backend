package vn.cineshow.dto.response.booking;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHold {
    private Long showtimeId;
    private Long userId;
    private List<SeatTicketDTO> seats;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
