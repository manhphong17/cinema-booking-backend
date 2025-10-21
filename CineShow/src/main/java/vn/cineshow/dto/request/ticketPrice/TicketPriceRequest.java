package vn.cineshow.dto.request.ticketPrice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.cineshow.enums.DayType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketPriceRequest {
    private Long seatTypeId;
    private Long roomTypeId;
    private DayType dayType; // NORMAL hoặc HOLIDAY
    private Double price;
}
