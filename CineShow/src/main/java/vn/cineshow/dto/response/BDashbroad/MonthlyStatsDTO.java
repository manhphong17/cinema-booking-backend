package vn.cineshow.dto.response.BDashbroad;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyStatsDTO {
    int month;
    double value;
}
