package vn.cineshow.dto.response.booking;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatTicketDTO {
    private Long ticketId;
    private Long seatId;
    private String seatType;
    private String status;
}
