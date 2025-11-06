package vn.cineshow.service;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.request.payment.CheckoutRequest;
import vn.cineshow.dto.response.payment.PaymentMethodDTO;

import java.util.List;
import java.util.Map;

public interface VNPayService {

    String createPaymentUrl(HttpServletRequest req, CheckoutRequest checkoutRequest);


    Map<String, String> handleIPN(Map<String, String> params);

    @Transactional(readOnly = true)
    Map<String, Object> handleReturn(Map<String, String> params);


}
