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
public class OperationDashboardInsightsResponse implements Serializable {
    private Long upcomingMoviesEnding;
    private Long roomsWithoutShowtime;
    private Long upcomingMoviesWithoutShowtime;
}

