package vn.cineshow.dto.response.order;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCheckTicketResponse {


    Long orderId;
    String orderCode;
    LocalDateTime createdAt;
    String userName;
    Long userId;
    Double totalPrice;
    String orderStatus;
    Integer ticketCount;
    Boolean isCheckIn;

    List<TicketInfo> tickets;
    List<OrderConcessionItem> concessions;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TicketInfo {
        Long ticketId;
        String seatCode;
        String seatType;
        Double ticketPrice;
        Long showtimeId;
        LocalDateTime showtimeStart;
        LocalDateTime showtimeEnd;
        String movieName;
        String posterUrl;
        String roomName;
    }
}



