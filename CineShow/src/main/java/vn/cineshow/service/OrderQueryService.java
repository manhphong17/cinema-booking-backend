package vn.cineshow.service;

import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import vn.cineshow.dto.request.order.QrRegenerateRequest;
import vn.cineshow.dto.response.order.OrderDetailResponse;
import vn.cineshow.dto.response.order.OrderListResponse;
import vn.cineshow.dto.response.order.ResendEmailResponse;

public interface OrderQueryService {
    OrderListResponse myOrders(Long currentUserId, Pageable pageable);
    OrderDetailResponse getOrderDetail(Long orderId, Long currentUserId);
    OrderDetailResponse regenerateQr(Long orderId, Long currentUserId, QrRegenerateRequest req);
    byte[] buildEticketPdf(Long orderId, Long currentUserId);
    void resendEmail(Long orderId, Long currentUserId);
    ResendEmailResponse resendETicket(Long orderId, String toEmail, String lang, Authentication auth);

}
