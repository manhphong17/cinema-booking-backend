package vn.cineshow.controller;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import vn.cineshow.dto.request.order.OrderCreatedAtSearchRequest;
import vn.cineshow.dto.request.order.OrderListRequest;
import vn.cineshow.dto.response.order.OrderDetailResponse;
import vn.cineshow.dto.response.order.OrderListItemResponse;
import vn.cineshow.dto.response.order.OrderListResponse;
import vn.cineshow.dto.response.order.OrderQrPayloadResponse;
import vn.cineshow.enums.OrderStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.Order;
import vn.cineshow.model.Seat;
import vn.cineshow.model.Ticket;
import vn.cineshow.repository.OrderRepository;
import vn.cineshow.service.OrderQueryService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")

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
                    .userName(o.getUser() != null ? o.getUser().getName() : null)
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

    //FIX //chưa test
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public OrderDetailResponse getOne(@PathVariable("id") Long id) {
        Order o = orderRepository.findOneById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        // ===== local helpers (chỉ dùng trong method này) =====
        java.util.function.Predicate<String> notBlank = s -> s != null && !s.isBlank();
        java.util.function.Function<Object, String> asString = v -> (v == null) ? null : String.valueOf(v);

        // Lấy code từ Ticket mà không cần biết getter chính xác (getCode / getTicketCode / getQrCode / getReservationCode)
        java.util.function.Function<Ticket, String> safeTicketCode = (Ticket t) -> {
            if (t == null) return null;
            String[] methods = {"getCode", "getTicketCode", "getQrCode", "getReservationCode"};
            for (String m : methods) {
                try {
                    var md = t.getClass().getMethod(m);
                    String val = asString.apply(md.invoke(t));
                    if (notBlank.test(val)) return val;
                } catch (Exception ignore) {}
            }
            return null;
        };

        // Lấy tên phương thức thanh toán: ưu tiên methodName, fallback code/methodCode/name
        java.util.function.Function<Object, String> safePaymentMethodName = (Object pm) -> {
            if (pm == null) return null;
            String[] methods = {"getMethodName", "getName", "getCode", "getMethodCode", "name"};
            for (String m : methods) {
                try {
                    var md = pm.getClass().getMethod(m);
                    String val = asString.apply(md.invoke(pm));
                    if (notBlank.test(val)) return val;
                } catch (Exception ignore) {}
            }
            return null;
        };
        // ===== end helpers =====

        String movie = safeMovieName(o);
        LocalDateTime start = resolveShowtimeStart(o);
        LocalDateTime end = resolveShowtimeEnd(o);
        String room = safeRoomName(o);
        List<String> seats = (o.getTickets() == null)
                ? List.of()
                : o.getTickets().stream().map(this::safeSeatLabel).toList();

        // --- reservationCode: ưu tiên từ Ticket, nếu trống thì lấy từ payments (transactionNo/txnRef) ---
        String reservationCode = null;
        if (o.getTickets() != null && !o.getTickets().isEmpty()) {
            Ticket t = pickPrimaryTicket(o);
            reservationCode = safeTicketCode.apply(t); // thay vì t.getCode()
        }
        if (!notBlank.test(reservationCode) && o.getPayments() != null && !o.getPayments().isEmpty()) {
            reservationCode = o.getPayments().stream()
                    .map(p -> {
                        String txNo = asString.apply(p.getTransactionNo()); // có thể là Long/Integer
                        String ref  = asString.apply(p.getTxnRef());        // thường là String
                        return notBlank.test(txNo) ? txNo : ref;
                    })
                    .filter(notBlank)
                    .findFirst()
                    .orElse(null);
        }

        // --- paymentMethods: ưu tiên methodName, fallback code/methodCode/name ---
        List<String> paymentMethods = (o.getPayments() == null) ? List.of()
                : o.getPayments().stream()
                .map(p -> safePaymentMethodName.apply(p.getMethod()))
                .filter(notBlank)
                .distinct()
                .toList();

        return OrderDetailResponse.builder()
                .orderId(o.getId())
                .createdAt(o.getCreatedAt())
                .userName(o.getUser() != null ? o.getUser().getName() : null)

                // NEW: trả về mã đơn hàng
                .orderCode(o.getCode())

                .bookingCode(reservationCode)
                .movieName(movie)
                .roomName(room)
                .showtimeStart(start)
                .showtimeEnd(end)
                .seats(seats)
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




    @GetMapping("/{id}/qr-payload")
    @Transactional(readOnly = true)
    public OrderQrPayloadResponse getQrPayload(@PathVariable("id") Long id) {
        Order o = orderRepository.findOneById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Order not found"));

        // ====== business guards (tùy bạn xử lý bằng AppException/ErrorCode) ======
        // ví dụ: nếu đã hủy / chưa thanh toán thì không phát QR
        if (o.getOrderStatus() == OrderStatus.CANCELED) {
            throw new AppException(ErrorCode.ORDER_CANCELED);
        }

        // ====== gather data (KHÔNG dùng ticket.code) ======
        String movie = safeMovieName(o);
        String room  = safeRoomName(o);
        LocalDateTime start = resolveShowtimeStart(o);
        LocalDateTime end   = resolveShowtimeEnd(o);
        List<String> seats = (o.getTickets() == null) ? List.of()
                : o.getTickets().stream().map(this::safeSeatLabel).toList();

        // reservationCode: ưu tiên payment.transactionNo / txnRef; nếu không có thì dùng order.code
        String reservationCode = null;
        if (o.getPayments() != null && !o.getPayments().isEmpty()) {
            reservationCode = o.getPayments().stream()
                    .map(p -> {
                        if (p.getTransactionNo() != null && !p.getTransactionNo().isBlank()) return p.getTransactionNo();
                        if (p.getTxnRef() != null && !p.getTxnRef().isBlank()) return p.getTxnRef();
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .findFirst().orElse(null);
        }
        if (reservationCode == null || reservationCode.isBlank()) {
            reservationCode = o.getCode() != null ? o.getCode() : String.valueOf(o.getId());
        }

        // ====== payload json (order-level) ======
        int graceMinutes = 30; // có thể load từ config
        long exp = Instant.now().plusSeconds(graceMinutes * 60L).getEpochSecond();
        String nonce = UUID.randomUUID().toString();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ver", 1); // versioning
        payload.put("nonce", nonce);
        payload.put("exp", exp);

        Map<String, Object> orderInfo = new LinkedHashMap<>();
        orderInfo.put("orderId", o.getId());
        orderInfo.put("orderCode", o.getCode());
        orderInfo.put("reservationCode", reservationCode);
        orderInfo.put("status", o.getOrderStatus() != null ? o.getOrderStatus().name() : null);
        payload.put("order", orderInfo);

        Map<String, Object> show = new LinkedHashMap<>();
        show.put("movie", movie);
        show.put("room", room);
        show.put("start", start != null ? start.toString() : null);
        show.put("end", end != null ? end.toString() : null);
        payload.put("showtime", show);

        payload.put("seats", seats); // ví dụ ["I7","I8","I9"]

        // ====== ký JWT tối giản (HS256) từ payload ======
        String jwt = createHs256Jwt(payload);

        // ====== derive additional fields for response ======
        List<String> ticketCodes = List.of();
        List<String> paymentMethods = (o.getPayments() == null) ? List.of()
                : o.getPayments().stream()
                .map(p -> {
                    Object m = p.getMethod();
                    return (m != null) ? String.valueOf(m) : null;
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Instant qrExpiryAt = Instant.ofEpochSecond(exp);
        String payloadJson = toJson(payload);

        return OrderQrPayloadResponse.builder()
                .orderId(o.getId())
                .userId(o.getUser() != null ? o.getUser().getId() : null)
                .createdAt(o.getCreatedAt())
                .totalPrice(o.getTotalPrice())
                .status(o.getOrderStatus() != null ? o.getOrderStatus().name() : null)
                .orderCode(o.getCode())
                .reservationCode(reservationCode)

                .movieName(movie)
                .roomName(room)
                .showtimeStart(start)
                .showtimeEnd(end)

                .seats(seats)
                .ticketCodes(ticketCodes)          // có thể là List.of()
                .paymentMethods(paymentMethods)    // nếu bạn đã tính

                .qrAvailable(true)
                .qrExpired(false)
                .regenerateAllowed(true)
                .graceMinutes(graceMinutes)
                .qrExpiryAt(qrExpiryAt)            // Instant bạn tính sẵn
                .qrJwt(jwt)
                .qrImageUrl(null)
                .payloadJson(payloadJson)

                .nonce(nonce)
                .version(1)
                .build();
    }

    /* ======================= Helpers ======================= */

    // secret để ký QR; cấu hình trong application.yml: qr.secret=your-256-bit-secret
    @Value("${qr.secret:change-this-secret}")
    private String qrSecret;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String toJson(Object o) {
        try { return MAPPER.writeValueAsString(o); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    // Tạo JWT HS256 tối giản: base64Url(header).base64Url(payload).base64Url(signature)
    private String createHs256Jwt(Object payload) {
        try {
            String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String payloadJson = toJson(payload);

            String headerB64  = base64Url(headerJson.getBytes(StandardCharsets.UTF_8));
            String payloadB64 = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
            String signingInput = headerB64 + "." + payloadB64;

            byte[] sig = hmacSha256(signingInput.getBytes(StandardCharsets.UTF_8), qrSecret.getBytes(StandardCharsets.UTF_8));
            String sigB64 = base64Url(sig);
            return signingInput + "." + sigB64;
        } catch (Exception e) {
            throw new RuntimeException("Cannot create QR JWT", e);
        }
    }

    private byte[] hmacSha256(byte[] data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }
    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

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
                    .userName(o.getUser() != null ? o.getUser().getName() : null)
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

        // Filter by userId first if provided, then by date
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
            List<String> seats = o.getTickets() == null ? List.of()
                    : o.getTickets().stream().map(this::safeSeatLabel).toList();

            return OrderListItemResponse.builder()
                    .orderId(o.getId())
                    .createdAt(o.getCreatedAt())
                    .userName(o.getUser() != null ? o.getUser().getName() : null)
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
