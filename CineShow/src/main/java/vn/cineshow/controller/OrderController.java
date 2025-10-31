package vn.cineshow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.order.OrderCreatedAtSearchRequest;
import vn.cineshow.dto.request.order.OrderListRequest;
import vn.cineshow.dto.response.order.OrderListItemResponse;
import vn.cineshow.dto.response.order.OrderListResponse;
import vn.cineshow.model.Order;
import vn.cineshow.model.Seat;
import vn.cineshow.model.Ticket;
import vn.cineshow.repository.OrderRepository;
import vn.cineshow.service.OrderQueryService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderQueryService orderQueryService;

    @GetMapping
    @Transactional(readOnly = true)
    public OrderListResponse listAll(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Order> page = orderRepository.findAllBy(pageable);

        List<OrderListItemResponse> items = page.getContent().stream().map(o -> {
            String movie = safeMovieName(o);
            LocalDateTime start = resolveShowtimeStart(o);
            String room = safeRoomName(o);
            List<String> seats = o.getTickets() == null ? List.of()
                    : o.getTickets().stream().map(this::safeSeatLabel).toList();

            return OrderListItemResponse.builder()
                    .orderId(o.getId())
                    .createdAt(o.getCreatedAt())
                    .movieName(movie)
                    .showtimeStart(start)
                    .roomName(room)
                    .seats(seats)
                    .totalPrice(o.getTotalPrice())
                    .status(o.getOrderStatus() != null ? o.getOrderStatus().name() : null)
                    .build();
        }).toList();

        return OrderListResponse.builder()
                .items(items)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

//    @GetMapping("/{id}")
//    @Transactional(readOnly = true)
//    public OrderDetailResponse getOne(@PathVariable("id") Long id) {
//        Order o = orderRepository.findOneById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
//
//        String movie = safeMovieName(o);
//        LocalDateTime start = resolveShowtimeStart(o);
//        LocalDateTime end = resolveShowtimeEnd(o);
//        String room = safeRoomName(o);
//        List<String> seats = (o.getTickets() == null)
//                ? List.of()
//                : o.getTickets().stream().map(this::safeSeatLabel).toList();
//
//        String reservationCode = null;
//        if (o.getTickets() != null && !o.getTickets().isEmpty()) {
//            Ticket t = pickPrimaryTicket(o);
//            reservationCode = (t != null) ? t.getCode() : null;
//        }
//        if ((reservationCode == null || reservationCode.isBlank())
//                && o.getPayments() != null && !o.getPayments().isEmpty()) {
//            reservationCode = o.getPayments().stream()
//                    .map(p -> p.getTransactionNo() != null ? p.getTransactionNo() : p.getTxnRef())
//                    .filter(s -> s != null && !s.isBlank())
//                    .findFirst()
//                    .orElse(null);
//        }
//
//        List<String> paymentMethods = (o.getPayments() == null) ? List.of()
//                : o.getPayments().stream()
//                .map(p -> p.getMethod() != null
//                        ? (p.getMethod().getMethodName() != null
//                        ? p.getMethod().getMethodName()
//                        : p.getMethod().getMethodCode())
//                        : null)
//                .filter(s -> s != null && !s.isBlank())
//                .distinct()
//                .toList();
//
//        return OrderDetailResponse.builder()
//                .orderId(o.getId())
//                .createdAt(o.getCreatedAt())
//                .userName(o.getUser() != null ? o.getUser().getName() : null)
//
//                // NEW: trả về mã đơn hàng
//                .orderCode(o.getCode())
//
//                .bookingCode(reservationCode)
//                .movieName(movie)
//                .roomName(room)
//                .showtimeStart(start)
//                .showtimeEnd(end)
//                .seats(seats)
//                .totalPrice(o.getTotalPrice())
//                .orderStatus(o.getOrderStatus() != null ? o.getOrderStatus().name() : null)
//                .reservationCode(reservationCode)
//                .paymentMethods(paymentMethods)
//                .qrAvailable(true)
//                .qrExpired(false)
//                .regenerateAllowed(false)
//                .qrJwt(null)
//                .qrImageUrl(null)
//                .graceMinutes(null)
//                .build();
//    }

   /* @PostMapping("/{id}/resend-email")
    @Transactional
    public ResendEmailResponse resendEmail(
            @PathVariable("id") Long id,
            @RequestBody(required = false) ResendEmailRequest req,
            Authentication auth
    ) {
        String toEmail = (req != null) ? req.getToEmail() : null;
        String language = (req != null) ? req.getLanguage() : null;
        return orderQueryService.resendETicket(id, toEmail, language, auth);
    }*/

    //    @GetMapping("/{id}/qr-payload")
//    @Transactional(readOnly = true)
//    public OrderQrPayloadResponse getQrPayload(@PathVariable("id") Long id) {
//        Order o = orderRepository.findOneById(id)
//                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Order not found"));
//
//        String movie = safeMovieName(o);
//        LocalDateTime start = resolveShowtimeStart(o);
//        String room = safeRoomName(o);
//        List<String> seats = o.getTickets() == null ? List.of()
//                : o.getTickets().stream().map(this::safeSeatLabel).toList();
//        List<String> ticketCodes = o.getTickets() == null ? List.of()
//                : o.getTickets().stream().map(t -> t.getCode()).toList();
//
//        Long userId = o.getUser() != null ? o.getUser().getId() : null;
//
//        return OrderQrPayloadResponse.builder()
//                .orderId(o.getId())
//                .userId(userId)
//                .createdAt(o.getCreatedAt())
//                .totalPrice(o.getTotalPrice())
//                .status(o.getOrderStatus() != null ? o.getOrderStatus().name() : null)
//                .orderCode(o.getCode())
//                .movieName(movie)
//                .showtimeStart(start)
//                .roomName(room)
//                .seats(seats)
//                .ticketCodes(ticketCodes)
//                .build();
//    }
    @PostMapping("/search")
    @Transactional(readOnly = true)
    public OrderListResponse search(@RequestBody OrderListRequest req) {
        int page = req.getPage() != null ? req.getPage() : 0;
        int size = req.getSize() != null ? req.getSize() : 10;

        Sort.Direction direction = Sort.Direction.DESC;
        List<String> sortList = req.getSort();
        if (sortList != null && !sortList.isEmpty()) {
            String first = sortList.get(0);
            if ("asc".equalsIgnoreCase(first)) {
                direction = Sort.Direction.ASC;
            } else if ("desc".equalsIgnoreCase(first)) {
                direction = Sort.Direction.DESC;
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(new Sort.Order(direction, "createdAt")));
        Page<Order> pageData = orderRepository.findAllBy(pageable);

        List<OrderListItemResponse> items = pageData.getContent().stream().map(o -> {
            String movie = safeMovieName(o);
            LocalDateTime start = resolveShowtimeStart(o);
            String room = safeRoomName(o);
            List<String> seats = (o.getTickets() == null)
                    ? List.of()
                    : o.getTickets().stream().map(this::safeSeatLabel).toList();

            return OrderListItemResponse.builder()
                    .orderId(o.getId())
                    .createdAt(o.getCreatedAt())
                    .movieName(movie)
                    .showtimeStart(start)
                    .code(o.getCode())                     // ⬅️ thêm dòng này
                    .roomName(room)
                    .seats(seats)
                    .totalPrice(o.getTotalPrice())
                    .status(o.getOrderStatus() != null ? o.getOrderStatus().name() : null)
                    .build();
        }).toList();

        return OrderListResponse.builder()
                .items(items)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .build();
    }

    @PostMapping("/search-by-date")
    @Transactional(readOnly = true)
    public OrderListResponse searchByCreated(@RequestBody OrderCreatedAtSearchRequest req) {
        if (req.getDate() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "date is required");
        }

        int page = req.getPage() != null ? req.getPage() : 0;
        int size = req.getSize() != null ? req.getSize() : 10;
        Sort.Direction direction = Sort.Direction.DESC;
        List<String> sortList = req.getSort();
        if (sortList != null && !sortList.isEmpty()) {
            String first = sortList.get(0);
            if (first != null && first.equalsIgnoreCase("asc")) {
                direction = Sort.Direction.ASC;
            } else if (first != null && first.equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(new Sort.Order(direction, "createdAt")));

        java.time.LocalDateTime start = req.getDate().atStartOfDay();
        java.time.LocalDateTime end = start.plusDays(1);

        Page<Order> pageData = orderRepository.findByCreatedAtBetween(start, end, pageable);

        List<OrderListItemResponse> items = pageData.getContent().stream().map(o -> {
            String movie = safeMovieName(o);
            LocalDateTime st = resolveShowtimeStart(o);
            String room = safeRoomName(o);
            List<String> seats = o.getTickets() == null ? List.of()
                    : o.getTickets().stream().map(this::safeSeatLabel).toList();

            return OrderListItemResponse.builder()
                    .orderId(o.getId())
                    .createdAt(o.getCreatedAt())
                    .movieName(movie)
                    .showtimeStart(st)
                    .roomName(room)
                    .seats(seats)
                    .totalPrice(o.getTotalPrice())
                    .status(o.getOrderStatus() != null ? o.getOrderStatus().name() : null)
                    .build();
        }).toList();

        return OrderListResponse.builder()
                .items(items)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .build();
    }


    private Ticket pickPrimaryTicket(Order order) {
        if (order == null || order.getTickets() == null || order.getTickets().isEmpty()) return null;
        return order.getTickets().stream()
                .min(Comparator.comparing(t -> {
                    Seat s = t.getSeat();
                    String label = s != null ? (Objects.toString(s.getRow(), "") + Objects.toString(s.getColumn(), "")) : "";
                    return label;
                }))
                .orElse(null);
    }

    private String safeMovieName(Order order) {
        Ticket t = pickPrimaryTicket(order);
        if (t == null || t.getShowTime() == null || t.getShowTime().getMovie() == null) return null;
        return t.getShowTime().getMovie().getName();
    }

    private LocalDateTime resolveShowtimeStart(Order order) {
        Ticket t = pickPrimaryTicket(order);
        return t != null && t.getShowTime() != null ? t.getShowTime().getStartTime() : null;
    }

    private LocalDateTime resolveShowtimeEnd(Order order) {
        Ticket t = pickPrimaryTicket(order);
        return t != null && t.getShowTime() != null ? t.getShowTime().getEndTime() : null;
    }

    private String safeRoomName(Order order) {
        Ticket t = pickPrimaryTicket(order);
        if (t == null || t.getShowTime() == null || t.getShowTime().getRoom() == null) return null;
        return t.getShowTime().getRoom().getName();
    }

    private String safeSeatLabel(Ticket t) {
        if (t == null || t.getSeat() == null) return null;
        String row = t.getSeat().getRow();
        String col = t.getSeat().getColumn();
        return (row != null ? row : "") + (col != null ? col : "");
    }
}
