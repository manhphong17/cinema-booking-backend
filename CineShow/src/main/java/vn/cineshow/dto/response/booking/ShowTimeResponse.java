package vn.cineshow.dto.response.booking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShowTimeResponse {
    private Long showTimeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long roomId;
    private String roomName;
    private String roomType;
    private Long totalSeat;
    private Long totalSeatAvailable;

    public ShowTimeResponse(Long showTimeId,
                            LocalDateTime startTime,
                            LocalDateTime endTime,
                            Long roomId,
                            String roomName,
                            String roomType,
                            Long totalSeat,
                            Long totalSeatAvailable) {
        this.showTimeId = showTimeId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomType = roomType;
        this.totalSeat = totalSeat != null ? totalSeat.longValue() : 0;
        this.totalSeatAvailable = totalSeatAvailable != null ? totalSeatAvailable.longValue() : 0;
    }
}
