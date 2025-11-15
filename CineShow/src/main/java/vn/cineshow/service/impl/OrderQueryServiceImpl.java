package vn.cineshow.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.config.SecurityAuditor;
import vn.cineshow.dto.request.order.OrderCreatedAtSearchRequest;
import vn.cineshow.dto.request.order.OrderListRequest;
import vn.cineshow.dto.response.order.*;
import vn.cineshow.enums.OrderStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.*;
import vn.cineshow.repository.OrderConcessionRepository;
import vn.cineshow.repository.OrderRepository;
import vn.cineshow.service.OrderQueryService;
import vn.cineshow.service.QrJwtService;
import vn.cineshow.service.QrTokenService;
import vn.cineshow.utils.validator.OwnershipValidator;
import vn.cineshow.utils.validator.QrPolicy;

import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j(topic = "ORDER-QUERY-SERVICE")
public class OrderQueryServiceImpl implements OrderQueryService {

    private static final Duration RESEND_DEBOUNCE = Duration.ofSeconds(60);

    private final OrderRepository orderRepository;
    private final OrderConcessionRepository orderConcessionRepository;
    private final OwnershipValidator ownershipValidator;
    private final QrPolicy qrPolicy;
    private final QrTokenService qrTokenService;
    private final QrJwtService qrJwtService;
    private final EmailServiceImpl emailService;
    private final SecurityAuditor securityAuditor;
    private final ConcurrentHashMap<String, Instant> resendDebounceMap = new ConcurrentHashMap<>();

    // ==================== Helper methods ====================

    private Ticket pickPrimaryTicket(Order order) {
        if (order == null || order.getTickets() == null || order.getTickets().isEmpty()) return null;
        return order.getTickets().stream()
                .min(Comparator.comparing(t -> {
                    Seat s = t.getSeat();
                    String label = s != null
                            ? (Objects.toString(s.getRow(), "") + Objects.toString(s.getColumn(), ""))
                            : "";
                    return label;
                }))
                .orElse(null);
    }

    private String safeMovieName(Order order) {
        Ticket t = pickPrimaryTicket(order);
        if (t == null) return null;
        ShowTime st = t.getShowTime();
        return st != null && st.getMovie() != null ? st.getMovie().getName() : null;
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
        if (t == null) return null;
        ShowTime st = t.getShowTime();
        Room room = st != null ? st.getRoom() : null;
        return room != null ? room.getName() : null;
    }

    // Convert 1 → A, 2 → B, 27 → AA...
    private String convertNumberToLetter(int number) {
        if (number < 1) return null;

        StringBuilder result = new StringBuilder();
        while (number > 0) {
            number--;
            result.insert(0, (char) ('A' + (number % 26)));
            number = number / 26;
        }
        return result.toString();
    }

    // Seat label dạng A1, B3... fallback row+col nếu không parse được
    private String safeSeatLabel(Ticket t) {
        if (t == null || t.getSeat() == null) return null;
        Seat s = t.getSeat();
        String row = s.getRow();
        String col = s.getColumn();
        if (row == null || col == null) return null;

        try {
            int rowNumber = Integer.parseInt(row);
            String rowLetter = convertNumberToLetter(rowNumber);
            return rowLetter + col;
        } catch (NumberFormatException e) {
            // Nếu row không phải số (đã là chữ), trả về row+col
            return row + col;
        }
    }

    private List<String> safeSeatLabels(Order order) {
        if (order.getTickets() == null) return List.of();
        return order.getTickets().stream()
                .map(this::safeSeatLabel)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
    }

    private boolean isQrExpired(LocalDateTime start, LocalDateTime now) {
        if (start == null) return true;
        return qrPolicy.isExpired(start, now);
    }

    private boolean canRegenerate(boolean paid, boolean canceled, LocalDateTime start, LocalDateTime now) {
        if (!paid || canceled || start == null) return false;
        return !qrPolicy.isExpired(start, now);
    }

    private String generateQrToken(Order order) {
        Ticket t = pickPrimaryTicket(order);
        if (t == null || t.getShowTime() == null) return null;
        Long showtimeId = t.getShowTime().getId();
        List<Long> seatIds = order.getTickets() == null ? List.of() : order.getTickets().stream()
                .map(Ticket::getSeat)
                .filter(Objects::nonNull)
                .map(Seat::getId)
                .collect(Collectors.toList());
        return qrTokenService.generateToken(order.getId(), showtimeId, seatIds, qrPolicy.graceMinutes(), "v1");
    }

    private OrderConcessionItem mapToOrderConcessionItem(OrderConcession oc) {
        if (oc == null || oc.getConcession() == null) return null;
        return OrderConcessionItem.builder()
                .name(oc.getConcession().getName())
                .quantity(oc.getQuantity())
                .unitPrice(oc.getUnitPrice())
                .urlImage(oc.getConcession().getUrlImage())
                .build();
    }

    private List<OrderConcessionItem> getConcessionsByOrderId(Long orderId) {
        try {
            List<OrderConcession> orderConcessions = orderConcessionRepository.findByOrderIdWithConcession(orderId);
            return orderConcessions.stream()
                    .map(this::mapToOrderConcessionItem)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private String safeTicketCode(Ticket t) {
        if (t == null) return null;
        java.util.function.Predicate<String> notBlank = s -> s != null && !s.isBlank();
        java.util.function.Function<Object, String> asString = v -> (v == null) ? null : String.valueOf(v);
        String[] methods = {"getCode", "getTicketCode", "getQrCode", "getReservationCode"};
        for (String m : methods) {
            try {
                var md = t.getClass().getMethod(m);
                String val = asString.apply(md.invoke(t));
                if (notBlank.test(val)) return val;
            } catch (Exception ignore) {}
        }
        return null;
    }

    private String safePaymentMethodName(Object pm) {
        if (pm == null) return null;
        java.util.function.Predicate<String> notBlank = s -> s != null && !s.isBlank();
        java.util.function.Function<Object, String> asString = v -> (v == null) ? null : String.valueOf(v);
        String[] methods = {"getMethodName", "getName", "getCode", "getMethodCode", "name"};
        for (String m : methods) {
            try {
                var md = pm.getClass().getMethod(m);
                String val = asString.apply(md.invoke(pm));
                if (notBlank.test(val)) return val;
            } catch (Exception ignore) {}
        }
        return null;
    }

    private OrderResponse mapToDTO(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .code(o.getCode())
                .customerName(o.getUser() != null ? o.getUser().getName() : "Khách tại quầy")
                .createdAt(o.getCreatedAt())
                .totalPrice(o.getTotalPrice() != null ? o.getTotalPrice() : 0.0)
                .status(o.getOrderStatus().name())
                .paymentMethod(
                        o.getPayment() != null ? o.getPayment().getMethod().getMethodName() : "N/A"
                )
                .build();
    }

    // ==================== Existing methods (merged) ====================

    @Override
    public Map<String, Object> getOrdersByStatus(String status, LocalDate date, int page, int size) {
        if (date == null) date = LocalDate.now();

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        OrderStatus filter = null;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            filter = OrderStatus.valueOf(status.toUpperCase());
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findAllByStatusAndDateRange(filter, startOfDay, endOfDay, pageable);

        Page<OrderResponse> orderDTOs = orders.map(this::mapToDTO);
        Map<String, Object> summary = getDailySummary(date);

        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderDTOs);
        response.put("summary", summary);
        return response;
    }

    @Override
    public Map<String, Object> getDailySummary(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenueToday",
                Optional.ofNullable(orderRepository.getRevenueByDate(startOfDay, endOfDay)).orElse(0.0));
        summary.put("totalTicketsSold",
                Optional.ofNullable(orderRepository.getTicketsByDate(startOfDay, endOfDay)).orElse(0L));
        summary.put("totalCompletedOrders",
                Optional.ofNullable(orderRepository.getCompletedOrdersByDate(startOfDay, endOfDay)).orElse(0L));
        summary.put("totalConcessionsSold",
                Optional.ofNullable(orderRepository.getConcessionSoldByDate(startOfDay, endOfDay)).orElse(0L));

        return summary;
    }

    @Override
    public OrderCheckTicketResponse checkTicketByOrderCode(String orderCode) {
        Order order = orderRepository.findByCodeWithTickets(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderCheckTicketResponse.TicketInfo> ticketInfos = order.getTickets() == null ? List.of() :
                order.getTickets().stream()
                        .map(ticket -> {
                            Seat seat = ticket.getSeat();
                            ShowTime showTime = ticket.getShowTime();
                            Movie movie = showTime != null ? showTime.getMovie() : null;
                            Room room = showTime != null ? showTime.getRoom() : null;

                            String seatCode = safeSeatLabel(ticket);
                            if (seatCode == null || seatCode.isEmpty()) {
                                throw new AppException(ErrorCode.TICKET_NOT_FOUND);
                            }

                            String seatType = (seat != null && seat.getSeatType() != null)
                                    ? seat.getSeatType().getName() : null;
                            Double ticketPrice = ticket.getPriceSnapshot() != null
                                    ? ticket.getPriceSnapshot()
                                    : (ticket.getTicketPrice() != null ? ticket.getTicketPrice().getPrice() : null);

                            return OrderCheckTicketResponse.TicketInfo.builder()
                                    .ticketId(ticket.getId())
                                    .seatCode(seatCode)
                                    .seatType(seatType)
                                    .ticketPrice(ticketPrice)
                                    .showtimeId(showTime != null ? showTime.getId() : null)
                                    .showtimeStart(showTime != null ? showTime.getStartTime() : null)
                                    .showtimeEnd(showTime != null ? showTime.getEndTime() : null)
                                    .movieName(movie != null ? movie.getName() : null)
                                    .posterUrl(movie != null ? movie.getPosterUrl() : null)
                                    .roomName(room != null ? room.getName() : null)
                                    .build();
                        })
                        .collect(Collectors.toList());

        return OrderCheckTicketResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getCode())
                .createdAt(order.getCreatedAt())
                .userName(order.getUser() != null ? order.getUser().getName() : null)
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : null)
                .ticketCount(ticketInfos.size())
                .isCheckIn(order.getIsCheckIn() != null ? order.getIsCheckIn() : false)
                .tickets(ticketInfos)
                .build();
    }

    @Override
    @Transactional
    public OrderCheckTicketResponse checkInOrder(Long orderId) {
        Order order = orderRepository.findOneById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.ORDER_NOT_COMPLETED);
        }

        if (Boolean.TRUE.equals(order.getIsCheckIn())) {
            throw new AppException(ErrorCode.ORDER_ALREADY_CHECKED_IN);
        }

        order.setIsCheckIn(true);
        orderRepository.save(order);

        return checkTicketByOrderCode(order.getCode());
    }

    @Override
    public OrderListResponse listAllOrders(Pageable pageable) {
        Page<Order> page = orderRepository.findAllBy(pageable);

        List<OrderListItemResponse> items = page.getContent().stream().map(o -> {
            String movie = safeMovieName(o);
            LocalDateTime start = resolveShowtimeStart(o);
            String room = safeRoomName(o);
            List<String> seats = safeSeatLabels(o);

            return OrderListItemResponse.builder()
                    .orderId(o.getId())
                    .createdAt(o.getCreatedAt())
                    .userName(o.getUser() != null ? o.getUser().getName() : null)
                    .movieName(movie)
                    .showtimeStart(start)
                    .code(o.getCode())
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

    @Override
    public OrderDetailResponse getOrderById(Long id) {
        Order o = orderRepository.findOneById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        java.util.function.Predicate<String> notBlank = s -> s != null && !s.isBlank();
        java.util.function.Function<Object, String> asString = v -> (v == null) ? null : String.valueOf(v);

        String movie = safeMovieName(o);
        LocalDateTime start = resolveShowtimeStart(o);
        LocalDateTime end = resolveShowtimeEnd(o);
        String room = safeRoomName(o);
        List<String> seats = safeSeatLabels(o);

        List<OrderConcessionItem> concessions = getConcessionsByOrderId(o.getId());

        String reservationCode = null;
        if (o.getTickets() != null && !o.getTickets().isEmpty()) {
            Ticket t = pickPrimaryTicket(o);
            reservationCode = safeTicketCode(t);
        }
        if (!notBlank.test(reservationCode) && o.getPayment() != null) {
            Payment p = o.getPayment();
            String txNo = asString.apply(p.getTransactionNo());
            String ref = asString.apply(p.getTxnRef());
            if (notBlank.test(txNo)) {
                reservationCode = txNo;
            } else if (notBlank.test(ref)) {
                reservationCode = ref;
            }
        }

        List<String> paymentMethods;
        if (o.getPayment() == null) {
            paymentMethods = List.of();
        } else {
            Payment p = o.getPayment();
            String method = safePaymentMethodName(p.getMethod());
            paymentMethods = notBlank.test(method) ? List.of(method) : List.of();
        }

        return OrderDetailResponse.builder()
                .orderId(o.getId())
                .createdAt(o.getCreatedAt())
                .userName(o.getUser() != null ? o.getUser().getName() : null)
                .orderCode(o.getCode())
                .bookingCode(reservationCode)
                .movieName(movie)
                .roomName(room)
                .showtimeStart(start)
                .showtimeEnd(end)
                .seats(seats)
                .concessions(concessions)
                .totalPrice(o.getTotalPrice())
                .orderStatus(o.getOrderStatus() != null ? o.getOrderStatus().name() : null)
                .reservationCode(reservationCode)
                .paymentMethods(paymentMethods)
                .qrAvailable(true)
                .qrExpired(false)
                .regenerateAllowed(false)
                .qrJwt(null)
                .qrImageUrl(null)
                .graceMinutes(null)
                .build();
    }

    @Override
    public OrderQrPayloadResponse getQrPayload(Long id) {
        Order o = orderRepository.findOneById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (o.getOrderStatus() == OrderStatus.CANCELED) {
            throw new AppException(ErrorCode.ORDER_CANCELED);
        }

        String movie = safeMovieName(o);
        String room = safeRoomName(o);
        LocalDateTime start = resolveShowtimeStart(o);
        LocalDateTime end = resolveShowtimeEnd(o);
        List<String> seats = safeSeatLabels(o);

        String reservationCode = null;
        Payment payment = o.getPayment();
        if (payment != null) {
            if (payment.getTransactionNo() != null && !payment.getTransactionNo().isBlank()) {
                reservationCode = payment.getTransactionNo();
            } else if (payment.getTxnRef() != null && !payment.getTxnRef().isBlank()) {
                reservationCode = payment.getTxnRef();
            }
        }

        if (reservationCode == null || reservationCode.isBlank()) {
            reservationCode = o.getCode() != null ? o.getCode() : String.valueOf(o.getId());
        }

        int graceMinutes = 30;
        long exp = Instant.now().plusSeconds(graceMinutes * 60L).getEpochSecond();
        String nonce = UUID.randomUUID().toString();

        String orderCode = o.getCode() != null && !o.getCode().isBlank()
                ? o.getCode()
                : String.valueOf(o.getId());

        Long userId = o.getUser() != null ? o.getUser().getId() : null;
        String userName = o.getUser() != null ? o.getUser().getName() : null;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ver", 1);
        payload.put("nonce", nonce);
        payload.put("exp", exp);
        payload.put("orderCode", orderCode);
        if (userName != null) payload.put("userName", userName);
        if (userId != null) payload.put("userId", userId);

        Map<String, Object> orderInfo = new LinkedHashMap<>();
        orderInfo.put("orderId", o.getId());
        orderInfo.put("orderCode", orderCode);
        orderInfo.put("reservationCode", reservationCode);
        orderInfo.put("status", o.getOrderStatus() != null ? o.getOrderStatus().name() : null);
        payload.put("order", orderInfo);

        Map<String, Object> show = new LinkedHashMap<>();
        show.put("movie", movie);
        show.put("room", room);
        show.put("start", start != null ? start.toString() : null);
        show.put("end", end != null ? end.toString() : null);
        payload.put("showtime", show);

        payload.put("seats", seats);

        String jwt = qrJwtService.createHs256Jwt(payload);

        List<String> ticketCodes = List.of();

        List<String> paymentMethods;
        if (o.getPayment() == null || o.getPayment().getMethod() == null) {
            paymentMethods = List.of();
        } else {
            PaymentMethod method = o.getPayment().getMethod();
            String methodName = String.valueOf(method.getMethodName());
            paymentMethods = List.of(methodName);
        }

        Instant qrExpiryAt = Instant.ofEpochSecond(exp);
        String payloadJson = qrJwtService.toJson(payload);

        return OrderQrPayloadResponse.builder()
                .orderId(o.getId())
                .userId(userId)
                .userName(userName)
                .createdAt(o.getCreatedAt())
                .totalPrice(o.getTotalPrice())
                .status(o.getOrderStatus() != null ? o.getOrderStatus().name() : null)
                .orderCode(orderCode)
                .reservationCode(reservationCode)
                .movieName(movie)
                .roomName(room)
                .showtimeStart(start)
                .showtimeEnd(end)
                .seats(seats)
                .ticketCodes(ticketCodes)
                .paymentMethods(paymentMethods)
                .qrAvailable(true)
                .qrExpired(false)
                .regenerateAllowed(true)
                .graceMinutes(graceMinutes)
                .qrExpiryAt(qrExpiryAt)
                .qrJwt(jwt)
                .qrImageUrl(null)
                .payloadJson(payloadJson)
                .nonce(nonce)
                .version(1)
                .build();
    }

    @Override
    public OrderListResponse searchOrders(OrderListRequest req) {
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
            List<String> seats = safeSeatLabels(o);

            return OrderListItemResponse.builder()
                    .orderId(o.getId())
                    .createdAt(o.getCreatedAt())
                    .userName(o.getUser() != null ? o.getUser().getName() : null)
                    .movieName(movie)
                    .showtimeStart(start)
                    .code(o.getCode())
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

    @Override
    public OrderListResponse searchOrdersByDate(OrderCreatedAtSearchRequest req) {
        if (req.getDate() == null) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
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

        LocalDateTime start = req.getDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        Page<Order> pageData;
        if (req.getUserId() != null) {
            pageData = orderRepository.findByUser_IdAndCreatedAtBetween(req.getUserId(), start, end, pageable);
        } else {
            pageData = orderRepository.findByCreatedAtBetween(start, end, pageable);
        }

        List<OrderListItemResponse> items = pageData.getContent().stream().map(o -> {
            String movie = safeMovieName(o);
            LocalDateTime st = resolveShowtimeStart(o);
            String room = safeRoomName(o);
            List<String> seats = safeSeatLabels(o);
            List<OrderConcessionItem> concessions = getConcessionsByOrderId(o.getId());

            return OrderListItemResponse.builder()
                    .orderId(o.getId())
                    .createdAt(o.getCreatedAt())
                    .userName(o.getUser() != null ? o.getUser().getName() : null)
                    .movieName(movie)
                    .showtimeStart(st)
                    .code(o.getCode())
                    .roomName(room)
                    .seats(seats)
                    .concessions(concessions)
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
}
