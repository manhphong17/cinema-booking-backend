package vn.cineshow.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayShowtimeResponse implements Serializable {
    private Long id;
    private String movieName;
    private String roomName;
    private String startTime;
    private String endTime;
    private String status; // "upcoming", "playing", "ended"
    private Double occupancyRate;
    private Long soldTickets;
    private Long totalCapacity;
}

