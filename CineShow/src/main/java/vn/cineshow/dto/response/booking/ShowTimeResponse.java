package vn.cineshow.dto.response.booking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShowTimeResponse {
    Long showTimeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long roomId;
    private String roomName;
    private String roomType;
    private Integer totalSeat;
    private Integer totalSeatAvailable;
}
