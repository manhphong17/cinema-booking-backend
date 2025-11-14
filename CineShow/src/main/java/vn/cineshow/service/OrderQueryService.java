package vn.cineshow.service;

import vn.cineshow.dto.response.order.OrderCheckTicketResponse;

import java.time.LocalDate;
import java.util.Map;

public interface OrderQueryService {


    Map<String, Object> getOrdersByStatus(String status, LocalDate date, int page, int size);

    Map<String, Object> getDailySummary(LocalDate date);

    OrderCheckTicketResponse checkTicketByOrderCode(String orderCode);
}
