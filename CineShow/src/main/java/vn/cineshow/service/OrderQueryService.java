package vn.cineshow.service;

import org.springframework.data.domain.Pageable;
import vn.cineshow.dto.request.order.OrderCreatedAtSearchRequest;
import vn.cineshow.dto.request.order.OrderListRequest;
import vn.cineshow.dto.response.order.OrderCheckTicketResponse;
import vn.cineshow.dto.response.order.OrderDetailResponse;
import vn.cineshow.dto.response.order.OrderListResponse;
import vn.cineshow.dto.response.order.OrderQrPayloadResponse;

import java.time.LocalDate;
import java.util.Map;

public interface OrderQueryService {

    Map<String, Object> getOrdersByStatus(String status, LocalDate date, int page, int size);

    Map<String, Object> getDailySummary(LocalDate date);

    OrderCheckTicketResponse checkTicketByOrderCode(String orderCode);

    // New methods moved from controller
    OrderListResponse listAllOrders(Pageable pageable);

    OrderDetailResponse getOrderById(Long id);

    OrderQrPayloadResponse getQrPayload(Long id);

    OrderListResponse searchOrders(OrderListRequest request);

    OrderListResponse searchOrdersByDate(OrderCreatedAtSearchRequest request);
}
