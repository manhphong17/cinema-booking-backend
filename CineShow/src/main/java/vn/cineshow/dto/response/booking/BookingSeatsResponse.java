package vn.cineshow.dto.response.booking;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingSeatsResponse implements Serializable {
    Long showTimeId;
    Long roomId;
    List<TicketResponse> ticketResponses;
}
