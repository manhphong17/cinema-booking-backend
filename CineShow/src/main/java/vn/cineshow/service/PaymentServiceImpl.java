package vn.cineshow.service;


import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import vn.cineshow.dto.request.payment.CheckoutRequest;

public interface PaymentServiceImpl {

    String createPaymentUrl(HttpServletRequest req, CheckoutRequest checkoutRequest);


    Map<String, String> handleIPN(Map<String, String> params);

    @Transactional(readOnly = true)
    Map<String, Object> handleReturn(Map<String, String> params);


    @Transactional
    Long createCashPayment(CheckoutRequest checkoutRequest);
}
