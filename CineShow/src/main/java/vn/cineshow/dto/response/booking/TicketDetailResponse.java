package vn.cineshow.dto.response.booking;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class TicketDetailResponse {

    Long ticketId;
    String seatCode;        // e.g., "C7"
    String seatType;        // e.g., "VIP"
    Double ticketPrice;     // from TicketPrice

    // Room info
    String roomName;
    String roomType;

    // ShowTime info
    Long showtimeId;
    String showDate;        // formatted yyyy-MM-dd
    String showTime;        // formatted HH:mm
    String hall;            // alias of room name

    // Movie info
    String movieName;
    String posterUrl;
}
