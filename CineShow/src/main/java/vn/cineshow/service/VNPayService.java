package vn.cineshow.service;


import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface VNPayService {
    Map<String, Object> createPaymentUrl(HttpServletRequest req, Long amount, String orderId);
    Map<String, String> handleIPN(Map<String, String> params);
    String handleReturn(Map<String, String> params);
}
