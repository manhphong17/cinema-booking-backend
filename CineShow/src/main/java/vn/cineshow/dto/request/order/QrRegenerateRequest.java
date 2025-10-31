package vn.cineshow.dto.request.order;

import lombok.Value;

@Value
public class QrRegenerateRequest {
    String reason; // optional, để log
    String idempotencyKey; // t
}
