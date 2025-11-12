package vn.cineshow.dto.response.BDashbroad;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessDashboardResponse {
    long revenueThisMonth;
    double revenueChange;
    long totalOrders;
    double orderChange;
    double avgOrderValue;
    double avgValueChange;


    List<MonthlyStatsDTO> revenueChart;
    List<MonthlyStatsDTO> orderChart;
    private List<TopProductDTO> topProducts;

}

