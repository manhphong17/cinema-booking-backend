package vn.cineshow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.order.OrderCreatedAtSearchRequest;
import vn.cineshow.dto.request.order.OrderListRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.order.*;
import vn.cineshow.service.OrderQueryService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderQueryService orderQueryService;

    @GetMapping
    public OrderListResponse listAll(
            @PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        return orderQueryService.listAllOrders(pageable);
    }

    @GetMapping("/{id}")
    public OrderDetailResponse getOne(@PathVariable("id") Long id) {
        return orderQueryService.getOrderById(id);
    }




    @GetMapping("/{id}/qr-payload")
    public OrderQrPayloadResponse getQrPayload(@PathVariable("id") Long id) {
        return orderQueryService.getQrPayload(id);
    }

    @PostMapping("/search")
    public OrderListResponse search(@RequestBody OrderListRequest req) {
        return orderQueryService.searchOrders(req);
    }

    @PostMapping("/search-by-date")
    public OrderListResponse searchByCreated(@RequestBody OrderCreatedAtSearchRequest req) {
        return orderQueryService.searchOrdersByDate(req);
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAnyAuthority('BUSINESS')")
    public ResponseData<Map<String, Object>> getOrdersByStatusAndDate(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Map<String, Object> data = orderQueryService.getOrdersByStatus(status, date, page, size);

        // lấy Page<OrderResponse> từ data
        Page<OrderResponse> ordersPage = (Page<OrderResponse>) data.get("orders");

        if (ordersPage == null || ordersPage.isEmpty()) {
            return new ResponseData<>(
                    HttpStatus.NOT_FOUND.value(),
                    "Không tìm thấy đơn hàng phù hợp trong ngày " + (date != null ? date : LocalDate.now())
            );
        }

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Fetched orders successfully",
                data // chứa cả "orders" và "summary"
        );
    }


    @GetMapping("/check-ticket")
    @PreAuthorize("hasAuthority('STAFF')")
    public ResponseData<OrderCheckTicketResponse> checkTicketByOrderCode(
            @RequestParam("orderCode") String orderCode
    ) {
        OrderCheckTicketResponse response = orderQueryService.checkTicketByOrderCode(orderCode);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Check ticket successfully. Total tickets: " + response.getTicketCount(),
                response
        );
    }

}
