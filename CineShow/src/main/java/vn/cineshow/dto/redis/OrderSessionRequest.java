package vn.cineshow.dto.redis;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.dto.request.booking.ConcessionOrderRequest;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderSessionRequest {
    Long userId;
    Long showtimeId;
    List<Long> ticketIds;
    List<ConcessionOrderRequest> combos;
}