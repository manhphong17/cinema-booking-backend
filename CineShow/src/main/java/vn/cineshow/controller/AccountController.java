package vn.cineshow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.dto.request.OtpVerifyDTO;
import vn.cineshow.dto.request.ResetPasswordRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.VerifyOtpResetResponse;
import vn.cineshow.exception.IllegalParameterException;
import vn.cineshow.service.AccountService;
import vn.cineshow.service.RegisterService;

import java.util.Optional;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final RegisterService registerService;
    private final AccountService accountService;

    // B1: Nhận info, lưu Redis, gửi OTP
    @PostMapping("/register-email")
    public ResponseData<?> registerEmail(@RequestBody @Valid EmailRegisterRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            throw new IllegalParameterException("password != confirmPassword");
        }

        long id = registerService.registerByEmail(req);
        return new ResponseData<>(HttpStatus.CREATED.value(),
                "Temp account created. Please verify OTP to activate.", id);
    }

    // B2: Verify OTP
    @PostMapping("/verify-otp")
    public ResponseData<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        registerService.verifyAccountAndUpdateStatus(email, otp);
        return new ResponseData<>(HttpStatus.OK.value(),
                "Account activated successfully", null);
    }

    // Quên mật khẩu → gửi OTP
    @PostMapping("/forgot-password")
    public ResponseData<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        boolean sent = accountService.forgotPassword(request);
        if (!sent) {
            return new ResponseData<>(HttpStatus.BAD_REQUEST.value(),
                    "Email not found or OTP could not be sent", null);
        }
        return new ResponseData<>(HttpStatus.OK.value(),
                "OTP sent to your email", null);
    }

    // B3: Verify OTP khi reset password
    @PostMapping("/verify-otp-reset")
    public ResponseData<VerifyOtpResetResponse> verifyOtpReset(@RequestBody @Valid OtpVerifyDTO req) {
        Optional<String> tokenOpt = accountService.verifyOtpForReset(req.email(), req.otpCode());
        if (!tokenOpt.isPresent()) {
            return new ResponseData<>(HttpStatus.BAD_REQUEST.value(), "Invalid or expired OTP", null);
        }
        return new ResponseData<>(HttpStatus.OK.value(),
                "OTP verified. Use this token to reset your password.",
                new VerifyOtpResetResponse(tokenOpt.get()));
    }

    // B4: Đặt lại mật khẩu bằng OTP
    @PostMapping("/reset-password")
    public ResponseData<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        boolean success = accountService.resetPassword(request);
        if (!success) {
            return new ResponseData<>(HttpStatus.BAD_REQUEST.value(),
                    "Invalid OTP or expired", null);
        }
        return new ResponseData<>(HttpStatus.OK.value(),
                "Password reset successfully", null);
    }

}


