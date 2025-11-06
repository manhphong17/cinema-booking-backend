package vn.cineshow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.*;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.SignInRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.SignInResponse;
import vn.cineshow.dto.response.TokenResponse;
import vn.cineshow.dto.response.VerifyOtpResetResponse;
import vn.cineshow.exception.IllegalParameterException;
import vn.cineshow.service.AccountService;
import vn.cineshow.service.AuthenticationService;
import vn.cineshow.service.OtpService;
import vn.cineshow.service.UserService;

import java.time.Duration;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication Controller")
@Slf4j(topic = "AUTHENTICATION-CONTROLLER")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    private final UserService userService;

    private final AccountService accountService;

    private final OtpService otpService;

    @Operation(summary = "Access token", description = "Get access token  email and password")
    @PostMapping("/log-in")
    public ResponseData<SignInResponse> getAccessToken(@RequestBody @Valid SignInRequest req, HttpServletResponse response) {
        log.info("Access token request:");
        TokenResponse tokenResponse = authenticationService.signIn(req);

        //set cookie for refresh token
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokenResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false)  // Fix: Set to false for localhost development
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Lax")  // Fix: Change from Strict to Lax for better compatibility
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        log.info("Access token response:" + tokenResponse.getAccessToken());
        //return access token + account info
        return new ResponseData<>(HttpStatus.OK.value(),
                "Login successful",
                SignInResponse.builder()
                        .accessToken(tokenResponse.getAccessToken())
                        .roleName(tokenResponse.getRoleNames())
                        .email(tokenResponse.getEmail())
                        .userId(tokenResponse.getUserId())
                        .build()
        );
    }

    @Operation(summary = "Refresh access token", description = "Get access token by refresh token when access token expired")
    @PostMapping("/refresh-token")
    public ResponseData<SignInResponse> refreshAccessToken(@CookieValue("refreshToken") String refreshToken) {
        log.info("Get new access token request:");
        SignInResponse tokenResponse = authenticationService.refresh(refreshToken);
        log.info("Generated new access token for user {}: {}", tokenResponse.getEmail(), tokenResponse.getAccessToken());
        return new ResponseData<>(HttpStatus.OK.value(),
                "Token refreshed successfully",
                tokenResponse
        );
    }

    @Operation(summary = "Resend verification email", description = "Resend account verification email")
    @PostMapping("/resend-verification")
    public ResponseData<String> resendVerification(@RequestParam @Email String email) {
        log.info("Resend verification email request: {}", email);
        String name = userService.getNameByAccountEmail(email);
        otpService.sendOtp(email, name);
        return new ResponseData<>(HttpStatus.OK.value(),
                "Verification email has been resent",
                email
        );
    }

    @Operation(summary = "Verify account", description = "Verify account with token from email")
    @PostMapping("/verify")
    public ResponseData<String> verifyAccount(@RequestParam String code, @RequestParam String email) {
        log.info("Verify account request with token: {}", code);

        otpService.verifyOtp(code, email);
        return new ResponseData<>(HttpStatus.OK.value(),
                "Account verified successfully"
        );
    }

    //=========================================================
    //======================= REGISTER =========================

    @PostMapping("/register-email")
    public ResponseData<?> registerEmail(@RequestBody @Valid EmailRegisterRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            throw new IllegalParameterException("password != confirmPassword");
        }

        long id = authenticationService.registerByEmail(req);
        return new ResponseData<>(HttpStatus.CREATED.value(),
                "Temp account created. Please verify OTP to activate.", id);
    }

    @PostMapping("/verify-otp")
    public ResponseData<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        authenticationService.verifyAccountAndUpdateStatus(email, otp);
        return new ResponseData<>(HttpStatus.OK.value(),
                "Account activated successfully", null);
    }

    @PostMapping("/resend-otp")
    public ResponseData<?> resendOtp(
            @RequestParam String email,
            @RequestParam String name
    ) {
        otpService.sendOtp(email, name);

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Mã OTP mới đã được gửi đến email của bạn.",
                null
        );
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
