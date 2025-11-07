package vn.cineshow.dto.response.order;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class OrderQrPayloadResponse {
    Long orderId;
    Long userId;
    LocalDateTime createdAt;
    Double totalPrice;
    String status;
    String orderCode;   // <— thêm field này
    String reservationCode;    // ⬅️ thêm: mã dùng để hiển thị/QR (txnNo/txnRef hoặc fallback)


    // -------- Showtime summary --------
    String movieName;
    String roomName;
    LocalDateTime showtimeStart;
    LocalDateTime showtimeEnd;  // optional: nếu bạn có

    List<String> seats;         // ví dụ ["I7","I8","I9"]
    List<String> ticketCodes;   // giữ lại nếu sau này có; hiện có thể rỗng
    List<String> paymentMethods;// tên/code phương thức thanh toán (nếu cần hiển thị)


    // -------- QR info --------
    boolean qrAvailable;        // có cho phép hiển thị QR không
    boolean qrExpired;          // đã hết hạn chưa (server-side tính)
    boolean regenerateAllowed;  // có cho phép tái tạo không (quota/time window)
    Integer graceMinutes;       // ví dụ 30
    Instant qrExpiryAt;         // thời điểm hết hạn QR (server gửi để FE render countdown)

    String qrJwt;               // token QR đã ký (HS256/JWT/HMAC…)
    String qrImageUrl;          // nếu render ra ảnh ở BE (có thể null)
    String payloadJson;         // JSON thô dùng để ký (debug/log/FE nếu cần)


    // -------- Misc / versioning --------
    String nonce;               // random để chống replay
    Integer version;            // version của payload (vd 1)

}
