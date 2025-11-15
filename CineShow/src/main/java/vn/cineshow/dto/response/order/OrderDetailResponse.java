package vn.cineshow.dto.response.order;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderDetailResponse {
    Long orderId;
    LocalDateTime createdAt;
    String userName;


    // NEW: mã đơn hàng (Order.code)
    String orderCode;   // <— thêm field này

    String bookingCode; // nếu bạn lưu ở Ticket/Payment có thể map sang đây
    String movieName;
    String roomName;
    LocalDateTime showtimeStart;
    LocalDateTime showtimeEnd;
    List<String> seats;
    List<OrderConcessionItem> concessions;
    Double totalPrice;
    String orderStatus;

    // Additional details for View Details
    String reservationCode; // ưu tiên ticket code, fallback txnRef/transactionNo
    List<String> paymentMethods; // tên hoặc code phương thức thanh toán

    // UI helpers
    boolean qrAvailable;
    boolean qrExpired;
    boolean regenerateAllowed;

    // QR data (tuỳ UI dùng image/url/svg…)
    String qrJwt;       // JWT/HMAC
    String qrImageUrl;  // nếu generate ra file/endpoint render ảnh
    Integer graceMinutes;
    Boolean isCheckIn;
}
