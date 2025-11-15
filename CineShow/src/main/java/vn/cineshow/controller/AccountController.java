package vn.cineshow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.ChangePasswordRequest;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.dto.request.OtpVerifyDTO;
import vn.cineshow.dto.request.ResetPasswordRequest;
import vn.cineshow.dto.request.account.AccountChangePasswordRequest;
import vn.cineshow.dto.request.account.AccountCreateRequest;
import vn.cineshow.dto.request.account.AccountUpdateRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.VerifyOtpResetResponse;
import vn.cineshow.dto.response.account.AccountResponse;
import vn.cineshow.exception.IllegalParameterException;
import vn.cineshow.service.AccountService;
import vn.cineshow.service.AuthenticationService;
import vn.cineshow.service.OtpService;

import java.util.Optional;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final OtpService otpService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register-email")
    public ResponseData<?> registerEmail(@RequestBody @Valid EmailRegisterRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            throw new IllegalParameterException("password != confirmPassword");
        }
        long id = authenticationService.registerByEmail(req);
        return new ResponseData<>(
                HttpStatus.CREATED.value(),
                "Temp account created. Please verify OTP to activate.",
                id
        );
    }

    @PostMapping("/verify-otp")
    public ResponseData<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        authenticationService.verifyAccountAndUpdateStatus(email, otp);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Account activated successfully",
                null
        );
    }

    @PostMapping("/resend-otp")
    public ResponseData<?> resendOtp(@RequestParam String email, @RequestParam String name) {
        otpService.sendOtp(email, name);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Mã OTP mới đã được gửi đến email của bạn.",
                null
        );
    }

    @PostMapping("/forgot-password")
    public ResponseData<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        boolean sent = accountService.forgotPassword(request);
        if (!sent) {
            return new ResponseData<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Email not found or OTP could not be sent",
                    null
            );
        }
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "OTP sent to your email",
                null
        );
    }

    @PostMapping("/verify-otp-reset")
    public ResponseData<VerifyOtpResetResponse> verifyOtpReset(@RequestBody @Valid OtpVerifyDTO req) {
        Optional<String> tokenOpt = accountService.verifyOtpForReset(req.email(), req.otpCode());
        if (!tokenOpt.isPresent()) {
            return new ResponseData<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid or expired OTP",
                    null
            );
        }
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "OTP verified. Use this token to reset your password.",
                new VerifyOtpResetResponse(tokenOpt.get())
        );
    }

    @PostMapping("/reset-password")
    public ResponseData<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        boolean success = accountService.resetPassword(request);
        if (!success) {
            return new ResponseData<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid OTP or expired",
                    null
            );
        }
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Password reset successfully",
                null
        );
    }

    @PostMapping("/{userId}/change-password")
    public ResponseData<?> changePassword(@PathVariable Long userId,
                                          @RequestBody @Valid ChangePasswordRequest request) {
        accountService.changePassword(userId, request);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Password changed successfully",
                null
        );
    }

    @PostMapping
    public ResponseData<AccountResponse> create(@RequestBody @Valid AccountCreateRequest req) {
        AccountResponse res = accountService.create(req);
        return new ResponseData<>(
                HttpStatus.CREATED.value(),
                "Account created",
                res
        );
    }

    @PutMapping("/{id}")
    public ResponseData<AccountResponse> update(@PathVariable Long id,
                                                @RequestBody @Valid AccountUpdateRequest req) {
        AccountResponse res = accountService.update(id, req);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Updated",
                res
        );
    }

    @PostMapping("/{id}/restore")
    public ResponseData<?> restore(@PathVariable Long id) {
        accountService.restore(id);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Restored",
                null
        );
    }

    @PostMapping("/{id}/admin-change-password")
    public ResponseData<?> adminChangePassword(@PathVariable Long id,
                                               @RequestBody @Valid AccountChangePasswordRequest req) {
        accountService.adminChangePassword(id, req);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Password changed (admin)",
                null
        );
    }

    @GetMapping
    public ResponseData<Page<AccountResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100));
        Page<AccountResponse> data = accountService.list(pageable);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "OK",
                data
        );
    }
}