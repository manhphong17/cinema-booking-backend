package vn.cineshow.service.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.cineshow.service.QrTokenService;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QrTokenServiceImpl implements QrTokenService {
    private final SecretKey secret = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final Set<String> blacklist = Collections.synchronizedSet(new HashSet<>());

    @Override
    public String generateToken(Long orderId, Long showtimeId, List<Long> seatIds, int ttlMinutes, String version) {
        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();
        return Jwts.builder()
                .setId(jti)
                .setSubject(String.valueOf(orderId))
                .claim("sti", showtimeId)
                .claim("sid", seatIds)
                .claim("ver", version)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlMinutes * 60L)))
                .signWith(secret)
                .compact();
    }

    @Override
    public boolean isBlacklisted(String jti) {
        return blacklist.contains(jti);
    }

    @Override
    public void blacklist(String jti) {
        blacklist.add(jti);
    }
}
