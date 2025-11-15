package vn.cineshow.service;

import java.util.Map;

public interface QrJwtService {
    /**
     * Tạo JWT HS256 từ payload map
     * @param payload Map chứa dữ liệu để ký
     * @return JWT string (header.payload.signature)
     */
    String createHs256Jwt(Map<String, Object> payload);

    /**
     * Chuyển đổi object sang JSON string
     * @param o Object cần chuyển đổi
     * @return JSON string
     */
    String toJson(Object o);
}

