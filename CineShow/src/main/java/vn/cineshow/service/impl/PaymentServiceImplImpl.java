package vn.cineshow.service.impl;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.cineshow.config.VNPayProperties;
import vn.cineshow.dto.redis.OrderSessionDTO;
import vn.cineshow.dto.request.payment.CheckoutRequest;
import vn.cineshow.enums.OrderStatus;
import vn.cineshow.enums.PaymentStatus;
import vn.cineshow.enums.TicketStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.Concession;
import vn.cineshow.model.Order;
import vn.cineshow.model.OrderConcession;
import vn.cineshow.model.Payment;
import vn.cineshow.model.PaymentMethod;
import vn.cineshow.model.Ticket;
import vn.cineshow.model.User;
import vn.cineshow.model.ids.OrderConcessionId;
import vn.cineshow.repository.ConcessionRepository;
import vn.cineshow.repository.OrderConcessionRepository;
import vn.cineshow.repository.OrderRepository;
import vn.cineshow.repository.PaymentMethodRepository;
import vn.cineshow.repository.PaymentRepository;
import vn.cineshow.repository.TicketRepository;
import vn.cineshow.repository.UserRepository;
import vn.cineshow.service.BookingService;
import vn.cineshow.service.RedisService;
import vn.cineshow.service.PaymentServiceImpl;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImplImpl implements PaymentServiceImpl {

    private final PaymentRepository paymentRepository; //
    private final VNPayProperties vnpayProperties;//
    private final OrderRepository orderRepository;
    private final OrderConcessionRepository orderConcessionRepository; //
    private final ConcessionRepository concessionRepository; //
    private final PaymentMethodRepository paymentMethodRepository; //
    private final TicketRepository ticketRepository; //
    private final UserRepository userRepository;//
    private final RedisService redisService; //
    private final BookingService bookingService; //


    @Value("${booking.ttl.payment}")
    long HOLD_DURATION;

    @Transactional
    @Override
        public String createPaymentUrl(HttpServletRequest req, CheckoutRequest checkoutRequest) {
        try {

            // 1️⃣ Lấy user
            User user = userRepository.findById(checkoutRequest.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            // 2️⃣ Tạo Order (chưa gắn ticket, chỉ setup metadata)
            Order order = Order.builder()
                    .user(user)
                    .totalPrice(checkoutRequest.getTotalPrice())
                    .discount(checkoutRequest.getDiscount())
                    .orderStatus(OrderStatus.PENDING)
                    .build();

            // 3️⃣ Lấy danh sách Ticket và gán luôn quan hệ hai chiều
            List<Ticket> tickets = new ArrayList<>(checkoutRequest.getTicketIds().size());
            for (Long id : checkoutRequest.getTicketIds()) {
                Ticket t = ticketRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));
                t.setOrder(order); // gán chiều ngược
                tickets.add(t);
            }
            order.setTickets(tickets);

            ticketRepository.saveAll(tickets);

            // 4️⃣ Lưu Order (cascade tickets nếu có CascadeType.MERGE)
            orderRepository.save(order);

            // 4️⃣ Tạo OrderConcession
            List<OrderConcession> orderConcessions = checkoutRequest.getConcessions().stream()
                    .map(cor -> {
                        Concession c = concessionRepository.findById(cor.getConcessionId())
                                .orElseThrow(() -> new AppException(ErrorCode.CONCESSION_NOT_FOUND));
                        return OrderConcession.builder()
                                .order(order)
                                .concession(c)
                                .orderConcessionId(new OrderConcessionId(order.getId(), c.getId()))
                                .quantity(cor.getQuantity())
                                .unitPrice(c.getPrice())
                                .priceSnapshot(c.getPrice() * cor.getQuantity())
                                .build();
                    })
                    .collect(Collectors.toList());
            orderConcessionRepository.saveAll(orderConcessions);

            // 5️⃣ Tạo Payment
            PaymentMethod paymentMethod = paymentMethodRepository
                    .findByPaymentCodeIgnoreCase(checkoutRequest.getPaymentCode())
                    .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

            Payment payment = Payment.builder()
                    .order(order)
                    .method(paymentMethod)
                    .amount(checkoutRequest.getAmount())
                    .txnRef(order.getCode())
                    .paymentStatus(PaymentStatus.PENDING)
                    .build();
//            paymentRepository.save(payment);
            order.setPayment(payment);

            // ORDER cascade payment, cascade orderConcession
            orderRepository.save(order);

            // 6️⃣ Cập nhật TTL cho OrderSession và SeatHold trong Redis
            Long userId = checkoutRequest.getUserId();
            Long showTimeId = checkoutRequest.getShowtimeId();

                // Build Redis keys
            String orderSessionKey = String.format("orderSession:showtime:%d:userId:%d", showTimeId, userId);
            String seatHoldKey = String.format("seatHold:showtime:%d:user:%d", showTimeId, userId);

            try {
                // ----  Cập nhật TTL cho OrderSession và SeatHold nếu tồn tại ----
                if (redisService.exists(orderSessionKey)) {
                    OrderSessionDTO session = redisService.get(orderSessionKey, OrderSessionDTO.class);

                    redisService.expire(orderSessionKey, Duration.ofSeconds(HOLD_DURATION));
                    redisService.save(orderSessionKey, session, HOLD_DURATION);
                    log.info("[REDIS TTL][ORDER_SESSION] Extended TTL for key={} to {} seconds", orderSessionKey, HOLD_DURATION);
                } else {
                    log.warn("[REDIS TTL][ORDER_SESSION] Key not found, cannot extend TTL: {}", orderSessionKey);
                }

                if (redisService.exists(seatHoldKey)) {
                    redisService.expire(seatHoldKey, Duration.ofSeconds(HOLD_DURATION));
                    log.info("[REDIS TTL][SEAT_HOLD] Extended TTL for key={} to {} seconds", seatHoldKey, HOLD_DURATION);
                } else {
                    log.warn("[REDIS TTL][SEAT_HOLD] Key not found, cannot extend TTL: {}", seatHoldKey);
                }

            } catch (Exception ex) {
                log.error("[REDIS UPDATE][ORDER_SESSION] Error updating TTL for user={}, showtime={}",
                        userId, showTimeId, ex);
            }

            //6. Build tham số gửi VNPay
            String vnp_IpAddr = req.getRemoteAddr();
            String orderInfo = "Thanh toan don hang: " + order.getCode();

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnpayProperties.getVersion());
            vnp_Params.put("vnp_Command", vnpayProperties.getCommand());
            vnp_Params.put("vnp_TmnCode", vnpayProperties.getTmnCode());
            vnp_Params.put("vnp_Amount", String.valueOf((long) (payment.getAmount() * 100)));
            if (!"CASH".equalsIgnoreCase(paymentMethod.getPaymentCode())) {
                vnp_Params.put("vnp_BankCode", paymentMethod.getPaymentCode());
            }
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", order.getCode());
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnpayProperties.getReturnUrl());
            vnp_Params.put("vnp_IpAddr", getIpAddress(req));

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String createDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", createDate);
            cld.add(Calendar.MINUTE, vnpayProperties.getTimeout());
            String expireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", expireDate);

            // 5️⃣ Sắp xếp & ký checksum
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            // Build hashData & query (encode value)
            StringJoiner hashPayload = new StringJoiner("&");
            StringJoiner query = new StringJoiner("&");

            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashPayload.add(fieldName + "=" + URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    query.add(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)
                            + "=" + URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                }
            }

            String vnp_SecureHash = hmacSHA512(vnpayProperties.getHashSecret(), hashPayload.toString());
            String paymentUrl = vnpayProperties.getPayUrl() + "?" + query + "&vnp_SecureHash=" + vnp_SecureHash;


            //8.trả về
            log.info("======== VNPay Checksum Debug ========");
            log.info("RAW DATA: {}", hashPayload);
            log.info("SECRET: {}", vnpayProperties.getHashSecret());
            log.info("SECURE HASH: {}", vnp_SecureHash);
            log.info("FULL PAY URL: {}", paymentUrl);
            log.info("======================================");
            return paymentUrl;

        } catch (AppException e) {
            // Trường hợp là lỗi nghiệp vụ (ví dụ: USER_NOT_FOUND, TICKET_NOT_FOUND)
            log.warn(" Business error in createPaymentUrl: {}", e.getMessage());
            throw e; // Giữ nguyên để GlobalExceptionHandler xử lý chuẩn

        } catch (Exception e) {
            // Trường hợp lỗi hệ thống
            log.error(" [VNPayService] Error creating payment URL: ", e);
            throw new RuntimeException("Lỗi hệ thống khi tạo URL thanh toán VNPay: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Map<String, String> handleIPN(Map<String, String> params) {
        Map<String, String> response = new HashMap<>();
        String orderSessionKey = null;
        String seatHoldKey = null;

        try {
            log.info("===== VNPay IPN Callback =====");
            log.info("Params: {}", params);

            // 1️⃣ Checksum validation
            if (!isValidChecksum(params)) {
                response.put("RspCode", "97");
                response.put("Message", "Invalid Checksum");
                return response;
            }

            // 2. Lấy payment từ DB theo mã txnRef
            String txnRef = params.get("vnp_TxnRef");
            Payment payment = (Payment) paymentRepository.findByTxnRef(txnRef).orElse(null);
            if (payment == null) {
                log.warn("Order not found for txnRef={}", txnRef);
                response.put("RspCode", "01");
                response.put("Message", "Order not Found");
                return response;
            }

            // 3. Chuẩn bị key Redis (xoá sau này)
            Order order = payment.getOrder();
            User user = order.getUser();
            List<Ticket> tickets = order.getTickets();

            Long userId = user.getId();
            Long showTimeId = tickets.get(0).getShowTime().getId();
            orderSessionKey = "orderSession:showtime:" + showTimeId + ":userId:" + userId;
            seatHoldKey = "seatHold:showtime:" + showTimeId + ":user:" + userId;

            // 4. Kiểm tra trạng thái payment
            if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
                log.info("Order already confirmed, status={}", payment.getPaymentStatus());
                response.put("RspCode", "02");
                response.put("Message", "Order already confirmed");
                return response;
            }

            // 5. Kiểm tra số tiền hợp lệ
            long amountFromVNPay = Long.parseLong(params.get("vnp_Amount")) / 100;
            if (amountFromVNPay != payment.getAmount().longValue()) {
                log.warn("Invalid Amount. DB={}, VNPAY={}", payment.getAmount(), amountFromVNPay);
                response.put("RspCode", "04");
                response.put("Message", "Invalid Amount");
                return response;
            }

            // 6. Lấy các mã trạng thái
            String responseCode = params.get("vnp_ResponseCode");
            String transactionStatus = params.get("vnp_TransactionStatus");
            String vnpTransactionNo = params.get("vnp_TransactionNo");

            List<OrderConcession> orderConcessions = order.getOrderConcession();

            // 8. Xử lý kết quả thanh toán
            if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
                payment.setTransactionNo(vnpTransactionNo);
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                order.setOrderStatus(OrderStatus.COMPLETED);

                for (Ticket ticket : tickets) {
                    ticket.setStatus(TicketStatus.BOOKED);
                    ticket.setPriceSnapshot(ticket.getTicketPrice().getPrice());
                }

                for (OrderConcession oc : orderConcessions) {
                    Concession concession = oc.getConcession();
                    concession.setUnitInStock(Math.max(concession.getUnitInStock() - oc.getQuantity(), 0));
                }

                int usedPoints = 0;
                if (order.getDiscount() != null && order.getDiscount() > 0) {
                    usedPoints = (int) (order.getDiscount() / 1000);
                    user.setLoyalPoint(Math.max(0, user.getLoyalPoint() - usedPoints));
                }

                int earnedPoints = (int) Math.floor(order.getTotalPrice() / 10000);
                int newPoints = Math.max(0, user.getLoyalPoint() - usedPoints + earnedPoints);

                user.setLoyalPoint(newPoints);

                orderRepository.save(order);
                userRepository.save(user);
                concessionRepository.saveAll(orderConcessions.stream().map(OrderConcession::getConcession).toList());

                // Broadcast booked seats via WebSocket
                List<Long> ticketIds = tickets.stream().map(Ticket::getId).toList();
                bookingService.broadcastBooked(showTimeId, ticketIds);

                log.info(" Payment SUCCESS — order={}, transactionNo={}", txnRef, vnpTransactionNo);
                response.put("RspCode", "00");
                response.put("Message", "Confirm Success");

            } else {
                payment.setTransactionNo(vnpTransactionNo);
                payment.setPaymentStatus(PaymentStatus.FAILED);
                order.setOrderStatus(OrderStatus.CANCELED);
                orderRepository.save(order);
                paymentRepository.save(payment);
                log.warn("Payment FAILED — order={}, code={}", txnRef, responseCode);
                response.put("RspCode", "00");
                response.put("Message", "Confirm Success");
            }

            return response;

        } catch (Exception e) {
            log.error("️[VNPayService] handleIPN error: ", e);
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
            return response;

        } finally {
            // Xoá key Redis dù có lỗi, return sớm hay exception
            //  Xoá key Redis nếu tồn tại
            if (orderSessionKey != null && redisService.exists(orderSessionKey)) {
                redisService.delete(orderSessionKey);
                log.info("[REDIS CLEANUP] Deleted orderSessionKey={}", orderSessionKey);
            } else if (orderSessionKey != null) {
                log.info("[REDIS CLEANUP] orderSessionKey={} not found (already expired or removed)", orderSessionKey);
            }

            if (seatHoldKey != null && redisService.exists(seatHoldKey)) {
                redisService.delete(seatHoldKey);
                log.info("[REDIS CLEANUP] Deleted seatHoldKey={}", seatHoldKey);
            } else if (seatHoldKey != null) {
                log.info("[REDIS CLEANUP] seatHoldKey={} not found (already expired or removed)", seatHoldKey);
            }
        }
    }


    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> handleReturn(Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();

        String orderSessionKey = null;
        String seatHoldKey = null;
        try {
            log.info("===== VNPay Return URL Callback =====");
            log.info("Params: {}", params);

            // 1  Checksum validation
            if (!isValidChecksum(params)) {
                response.put("RspCode", "97");
                response.put("Message", "Invalid Checksum");
                return response;
            }

            // 2. Lấy thông tin giao dịch
            String txnRef = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            String transactionStatus = params.get("vnp_TransactionStatus");
            String vnpTransactionNo = params.get("vnp_TransactionNo");

            Payment payment = (Payment) paymentRepository.findByTxnRef(txnRef).orElse(null);
            if (payment == null) {
                log.warn(" Payment not found for txnRef={}", txnRef);
                response.put("status", "FAILED");
                response.put("message", "Order not found");
                return response;
            }

            // 3. Chuẩn bị key Redis (xoá sau này)
            Order order = payment.getOrder();
            User user = order.getUser();
            List<Ticket> tickets = order.getTickets();

            Long userId = user.getId();
            Long showTimeId = tickets.get(0).getShowTime().getId();
            orderSessionKey = "orderSession:showtime:" + showTimeId + ":userId:" + userId;
            seatHoldKey = "seatHold:showtime:" + showTimeId + ":user:" + userId;

            // 4. Xử lý hiển thị
            if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
                boolean dbCompleted =
                        order.getOrderStatus() == OrderStatus.COMPLETED &&
                                payment.getPaymentStatus() == PaymentStatus.COMPLETED;

                if (dbCompleted) {
                    response.put("status", "SUCCESS");
                    response.put("message", "Thanh toán thành công");
                } else {
                    // VNPay claims success but DB not updated yet (IPN not received)
                    log.warn("Return URL success but DB not updated — txnRef={}, orderStatus={}, paymentStatus={}",
                            txnRef, order.getOrderStatus(), payment.getPaymentStatus());
                    response.put("status", "FAILED");
                    response.put("message", "Thanh toán không thành công hoặc đã hết hạn thanh toán");
                }
            } else {
                response.put("status", "FAILED");
                response.put("message", "Thanh toán không thành công hoặc đã hết hạn thanh toán");
            }
            response.put("orderCode", payment.getTxnRef());
            return response;

        } catch (Exception e) {
            log.error("️ [VNPayService] handleReturn error: ", e);
            response.put("status", "FAILED");
            response.put("message", "Unknown error during return processing");
            return response;
        }finally {
            // Xoá key Redis dù có lỗi, return sớm hay exception
            if (orderSessionKey != null && redisService.exists(orderSessionKey)) {
                redisService.delete(orderSessionKey);
                log.info("[REDIS CLEANUP] Deleted orderSessionKey={}", orderSessionKey);
            } else if (orderSessionKey != null) {
                log.info("[REDIS CLEANUP] orderSessionKey={} not found (already expired or removed)", orderSessionKey);
            }

            if (seatHoldKey != null && redisService.exists(seatHoldKey)) {
                redisService.delete(seatHoldKey);
                log.info("[REDIS CLEANUP] Deleted seatHoldKey={}", seatHoldKey);
            } else if (seatHoldKey != null) {
                log.info("[REDIS CLEANUP] seatHoldKey={} not found (already expired or removed)", seatHoldKey);
            }
        }
    }


    private  String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String getIpAddress(HttpServletRequest req) {
        // Lấy IP thực tế từ header (nếu chạy qua proxy/ngrok)
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = req.getRemoteAddr();
        }

        //  Nếu là IPv6 localhost thì ép sang IPv4
        if (ip == null || ip.isBlank() ||
                "0:0:0:0:0:0:0:1".equals(ip) ||
                "::1".equals(ip)) {
            ip = "127.0.0.1";
        }

        //  Trường hợp có nhiều IP (ngăn cách dấu ,)
        if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip.trim();
    }

    /**
     * Build sorted query string (hashData) from VNPay params.
     */
    private String buildHashData(Map<String, String> params) {
        Map<String, String> fields = new HashMap<>(params);
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringJoiner hashData = new StringJoiner("&");
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.add(fieldName + "=" + URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            }
        }
        return hashData.toString();
    }

    /**
     * Validate VNPay checksum.
     * @return true if checksum is valid, false otherwise.
     */
    private boolean isValidChecksum(Map<String, String> params) {
        String hashData = buildHashData(params);
        String localHash = hmacSHA512(vnpayProperties.getHashSecret(), hashData);
        String receivedHash = params.get("vnp_SecureHash");
        boolean isValid = localHash.equalsIgnoreCase(receivedHash);

        if (!isValid) {
            log.warn("Checksum mismatch — expected={}, received={}", localHash, receivedHash);
        }
        return isValid;
    }

    @Transactional
    @Override
    public void createCashPayment(CheckoutRequest checkoutRequest) {
        log.info(" Bắt đầu thanh toán CASH ");

        // 1️⃣ Tạo Order (KHÔNG gắn user, status COMPLETED)
        Order order = Order.builder()
                .totalPrice(checkoutRequest.getTotalPrice())
                .discount(checkoutRequest.getDiscount())
                .orderStatus(OrderStatus.COMPLETED)
                .build();

        // 2️⃣ Lấy danh sách Ticket và gán hai chiều + BOOKED
        List<Ticket> tickets = new ArrayList<>(checkoutRequest.getTicketIds().size());
        for (Long id : checkoutRequest.getTicketIds()) {
            Ticket t = ticketRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));
            t.setOrder(order);
            t.setStatus(TicketStatus.BOOKED);
            t.setPriceSnapshot(t.getTicketPrice().getPrice()); // snapshot giá vé tại thời điểm thanh toán
            tickets.add(t);
        }
        order.setTickets(tickets);
        ticketRepository.saveAll(tickets);

        // 3️⃣ Tạo OrderConcession + trừ stock
        List<OrderConcession> orderConcessions = new ArrayList<>();
        if (checkoutRequest.getConcessions() != null) {
            for (CheckoutRequest.ConcessionOrderRequest req : checkoutRequest.getConcessions()) {
                Concession concession = concessionRepository.findById(req.getConcessionId())
                        .orElseThrow(() -> new AppException(ErrorCode.CONCESSION_NOT_FOUND));

                // Trừ stock
                int remain = concession.getUnitInStock() - req.getQuantity();
                concession.setUnitInStock(Math.max(remain, 0));
                concessionRepository.save(concession);

                // Tạo OrderConcession record
                OrderConcession oc = OrderConcession.builder()
                        .order(order)
                        .concession(concession)
                        .orderConcessionId(new OrderConcessionId(order.getId(), concession.getId()))
                        .quantity(req.getQuantity())
                        .unitPrice(concession.getPrice())
                        .priceSnapshot(concession.getPrice() * req.getQuantity())
                        .build();
                orderConcessions.add(oc);
            }
            orderConcessionRepository.saveAll(orderConcessions);
        }


        // 4️⃣ Tạo Payment (status COMPLETED)
        PaymentMethod method = paymentMethodRepository
                .findByPaymentCodeIgnoreCase(checkoutRequest.getPaymentCode())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

        String transactionNo = "CASH-" + String.format("%08d", System.currentTimeMillis() % 100_000_000);

        Payment payment = Payment.builder()
                .order(order)
                .method(method)
                .amount(checkoutRequest.getAmount())
                .txnRef(order.getCode())
                .transactionNo(transactionNo)
                .paymentStatus(PaymentStatus.COMPLETED)
                .build();

        order.setPayment(payment);

        // 5️⃣ Lưu toàn bộ
        orderRepository.save(order);


        // --- 6. Xóa key Redis (OrderSession + SeatHold) ---
        try {
            String orderSessionKey = "order_session:" + checkoutRequest.getShowtimeId() + ":" + checkoutRequest.getUserId();
            String seatHoldKey = "seat_hold:" + checkoutRequest.getShowtimeId() + ":" + checkoutRequest.getUserId();
            redisService.delete(orderSessionKey);
            redisService.delete(seatHoldKey);
            log.info("Xóa key Redis: {}, {}", orderSessionKey, seatHoldKey);
        } catch (Exception e) {
            log.warn("Không thể xóa key Redis: {}", e.getMessage());
        }

        log.info(" Thanh toán CASH hoàn tất cho đơn hàng {}", order.getCode());
    }
}


