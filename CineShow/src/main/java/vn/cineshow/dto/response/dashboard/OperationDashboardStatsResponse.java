package vn.cineshow.dto.response.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperationDashboardStatsResponse implements Serializable {
    private MovieStatsResponse movieStats;
    private ShowtimeStatsResponse showtimeStats;
    private RoomStatsResponse roomStats;
    private List<TodayShowtimeResponse> todayShowtimes;
    private List<PlayingMovieResponse> playingMovies;
    private List<HotMovieResponse> hotMovies;
    private List<OperationDashboardAlertResponse> alerts;
    private OperationDashboardInsightsResponse insights;
}

