package vn.cineshow.dto.response.booking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import vn.cineshow.dto.response.showtime.ShowTimeResponse;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieShowTimesResponse {
    public List<ShowTimeResponse> showTimes;

}
