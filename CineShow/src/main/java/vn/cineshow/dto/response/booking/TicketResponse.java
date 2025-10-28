package vn.cineshow.dto.response.booking;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TicketResponse implements Serializable {
    private Long ticketId;
    private Long seatId;
    private int rowIdx;
    private int columnInx;
    private String seatType;
    private String seatStatus;
    private Double ticketPrice;
}
