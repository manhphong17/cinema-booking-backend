package vn.cineshow.dto.response.ticketPrice;



import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketPriceResponse {
    private Long roomTypeId;
    private Long seatTypeId;
    private Double normalDayPrice;
    private Double weekendPrice;
}
