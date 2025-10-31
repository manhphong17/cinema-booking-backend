package vn.cineshow.service;

import java.util.List;

public interface QrTokenService {
    String generateToken(Long orderId, Long showtimeId, List<Long> seatIds, int ttlMinutes, String version);
    boolean isBlacklisted(String jti);
    void blacklist(String jti);
}
