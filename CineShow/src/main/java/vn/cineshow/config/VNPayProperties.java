package vn.cineshow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vnpay")
@Getter
@Setter
public class VNPayProperties {
    private String tmnCode;       // Mã terminal của merchant (VNPAY cấp)
    private String hashSecret;    // Khóa bí mật để tạo checksum
    private String payUrl;        // URL cổng thanh toán VNPay
    private String returnUrl;     // URL redirect sau khi thanh toán
    private String version;       // Phiên bản API (vd: 2.1.0)
    private Integer timeout;      // Thời gian hết hạn thanh toán (phút)
    private String command;       // Mã API của VNPay, mặc định: "pay"

}
