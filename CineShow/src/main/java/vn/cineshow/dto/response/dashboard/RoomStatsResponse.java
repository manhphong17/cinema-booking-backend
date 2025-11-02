package vn.cineshow.dto.response.dashboard;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatsResponse implements Serializable {
    private Long total;
    private Long active;
    private Long inactive;
}

