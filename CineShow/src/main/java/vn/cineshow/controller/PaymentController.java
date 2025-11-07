package vn.cineshow.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.payment.CheckoutRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.service.VNPayService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "Handle VNPay and checkout flow")
@Slf4j
public class PaymentController {

    private final VNPayService vnpayService;

    @PostMapping("/checkout")

    public ResponseData<String> createVNPayPayment(HttpServletRequest request,
                                                   @RequestBody CheckoutRequest checkoutRequest) {

        String paymentUrl = vnpayService.createPaymentUrl(request, checkoutRequest);
        return new ResponseData<>(HttpStatus.OK.value(), "Create VNPay URL successfully", paymentUrl);

    }

    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> handleVNPayIPN(@RequestParam Map<String, String> params) {
        log.info(" Received VNPay IPN callback with params: {}", params);

        Map<String, String> response = vnpayService.handleIPN(params);

        log.info("📤 Responding to VNPay IPN: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/return")
    public ResponseData<Map<String, Object>> handleReturn(HttpServletRequest request) {
        // Convert all query params to a Map<String, String>
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> {
            if (value != null && value.length > 0) {
                params.put(key, value[0]);
            }
        });

        Map<String, Object> result = vnpayService.handleReturn(params);

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "VNPay return processed",
                result
        );
    }

}
