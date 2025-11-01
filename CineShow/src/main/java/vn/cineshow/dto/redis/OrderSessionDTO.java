package vn.cineshow.dto.redis;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.OrderSessionStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderSessionDTO implements Serializable {

    Long userId;
    Long showtimeId;
    List<Long> ticketIds;
    List<ConcessionOrderRequest> concessionOrders;
    Double totalPrice;
    OrderSessionStatus status;
    LocalDateTime createdAt;
    LocalDateTime expiredAt;
}
