package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.response.BDashbroad.BusinessDashboardResponse;
import vn.cineshow.dto.response.BDashbroad.TopProductDTO;
import vn.cineshow.repository.OrderRepository;
import vn.cineshow.repository.UserRepository;
import vn.cineshow.service.BusinessDashboardService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BusinessDashboardServiceImpl implements BusinessDashboardService {

    private final OrderRepository orderRepository;

    @Override
    public BusinessDashboardResponse getBusinessDashboard() {
        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(now);
        YearMonth prevMonth = currentMonth.minusMonths(1);

        // --- Doanh thu tháng này & tháng trước ---
        long revenueThisMonth = Optional.ofNullable(orderRepository
                        .sumRevenueByMonth(currentMonth.getYear(), currentMonth.getMonthValue()))
                .orElse(0L);
        long revenueLastMonth = Optional.ofNullable(orderRepository
                        .sumRevenueByMonth(prevMonth.getYear(), prevMonth.getMonthValue()))
                .orElse(0L);
        double revenueChange = calculateChangePercent(revenueThisMonth, revenueLastMonth);

        // --- Tổng đơn hàng ---
        long totalOrders = Optional.ofNullable(orderRepository
                        .countByMonth(currentMonth.getYear(), currentMonth.getMonthValue()))
                .orElse(0L);
        long prevOrders = Optional.ofNullable(orderRepository
                        .countByMonth(prevMonth.getYear(), prevMonth.getMonthValue()))
                .orElse(0L);

        double orderChange = calculateChangePercent(totalOrders, prevOrders);


        // --- Giá trị đơn TB ---
        double avgOrderValue = (totalOrders > 0) ? (double) revenueThisMonth / totalOrders : 0;
        double prevAvgValue = (prevOrders > 0) ? (double) revenueLastMonth / prevOrders : 0;
        double avgValueChange = calculateChangePercent(avgOrderValue, prevAvgValue);

//
//        // --- Dữ liệu biểu đồ trong năm hiện tại (dữ liệu 12 tháng nếu lấy dữ liệu cho đến tháng hiện tại, do vậy nếu bh là tháng 11 thì chỉ hiện 11 cột) ---
        var revenueChart = Optional.ofNullable(orderRepository.getRevenueOfCurrentYear()).orElse(List.of());
        var orderChart = Optional.ofNullable(orderRepository.getOrdersOfCurrentYear()).orElse(List.of());
        var topProducts = orderRepository.findTopProductsOfCurrentMonth(PageRequest.of(0, 4));

        return BusinessDashboardResponse.builder()
                .revenueThisMonth(revenueThisMonth)
                .revenueChange(revenueChange)
                .totalOrders(totalOrders)
                .orderChange(orderChange)
                .avgOrderValue(avgOrderValue)
                .avgValueChange(avgValueChange)
                .revenueChart(revenueChart)
                .orderChart(orderChart)
                .topProducts(topProducts)
                .build();
    }

    private double calculateChangePercent(double current, double previous) {
        if (previous == 0) return 0;
        return ((current - previous) / previous) * 100.0;
    }

    public List<TopProductDTO> getTopProducts() {
        Pageable top4 = PageRequest.of(0, 4);
        return orderRepository.findTopProductsOfCurrentMonth(top4);
    }
}
