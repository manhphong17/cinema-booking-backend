package vn.cineshow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.cineshow.service.QrJwtService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QrJwtServiceImpl implements QrJwtService {

    @Value("${qr.secret:change-this-secret}")
    private String qrSecret;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String toJson(Object o) {
        try {
            return MAPPER.writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException("Cannot convert object to JSON", e);
        }
    }

    @Override
    public String createHs256Jwt(Map<String, Object> payload) {
        try {
            String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String payloadJson = toJson(payload);

            String headerB64 = base64Url(headerJson.getBytes(StandardCharsets.UTF_8));
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
}

