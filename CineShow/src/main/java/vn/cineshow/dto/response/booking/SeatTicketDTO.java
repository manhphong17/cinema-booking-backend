package vn.cineshow.dto.response.booking;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SeatTicketDTO {
    private Long ticketId;
    private int rowIdx;
    private int columnIdx;
    private String seatType;
    private String status;
}
