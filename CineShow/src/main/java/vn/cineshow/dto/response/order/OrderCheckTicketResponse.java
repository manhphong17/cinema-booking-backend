package vn.cineshow.dto.response.order;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

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
    Double totalPrice;
    String orderStatus;
    Integer ticketCount;

    List<TicketInfo> tickets;

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

        // ShowTime info
        Long showtimeId;
        LocalDateTime showtimeStart;
        LocalDateTime showtimeEnd;

        // Movie info
        String movieName;
        String posterUrl;

        // Room info
        String roomName;
    }
}



