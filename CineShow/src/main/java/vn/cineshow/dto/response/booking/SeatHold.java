package vn.cineshow.dto.response.booking;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("SeatHold")
public class SeatHold {

    @Id
    private String id;

    @Indexed
    private Long showtimeId;

    @Indexed
    private Long userId;
    private List<SeatTicketDTO> seats;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @TimeToLive
    private Long ttl;


}
