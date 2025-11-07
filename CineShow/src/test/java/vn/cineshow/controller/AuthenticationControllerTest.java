package vn.cineshow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.dto.request.OtpVerifyDTO;
import vn.cineshow.dto.request.ResetPasswordRequest;
import vn.cineshow.dto.request.SignInRequest;
import vn.cineshow.dto.response.SignInResponse;
import vn.cineshow.dto.response.TokenResponse;
import vn.cineshow.service.AccountService;
import vn.cineshow.service.AuthenticationService;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.OtpService;
import vn.cineshow.service.UserService;
import vn.cineshow.service.impl.AccountDetailsService;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private UserService userService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private OtpService otpService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AccountDetailsService accountDetailsService;

    // ==================== POST /auth/log-in ====================
    @Test
    @DisplayName("POST /auth/log-in should return access token and set refresh token cookie")
    void getAccessToken_shouldReturnAccessTokenAndCookie() throws Exception {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setEmail("user@test.com");
        signInRequest.setPassword("password123");

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("access-token-123")
                .refreshToken("refresh-token-123")
                .email("user@test.com")
                .userId(1L)
                .roleNames(Arrays.asList("CUSTOMER"))
                .build();

        when(authenticationService.signIn(any(SignInRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/auth/log-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.email").value("user@test.com"))
                .andExpect(jsonPath("$.data.roleName").isArray())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().value("refreshToken", "refresh-token-123"));

        verify(authenticationService, times(1)).signIn(any(SignInRequest.class));
    }

    @Test
    @DisplayName("POST /auth/log-in should fail with invalid credentials")
    void getAccessToken_invalidCredentials_shouldFail() throws Exception {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setEmail("user@test.com");
        signInRequest.setPassword("wrongpassword");

        when(authenticationService.signIn(any(SignInRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/auth/log-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().is5xxServerError());

        verify(authenticationService, times(1)).signIn(any(SignInRequest.class));
    }

    // ==================== POST /auth/refresh-token ====================
    @Test
    @DisplayName("POST /auth/refresh-token should refresh access token successfully")
    void refreshAccessToken_shouldRefreshSuccessfully() throws Exception {
        String refreshToken = "refresh-token-123";
        SignInResponse signInResponse = SignInResponse.builder()
                .accessToken("new-access-token-123")
                .email("user@test.com")
                .userId(1L)
                .roleName(Arrays.asList("CUSTOMER"))
                .build();

        when(authenticationService.refresh(refreshToken)).thenReturn(signInResponse);

        org.springframework.mock.web.MockCookie cookie = new org.springframework.mock.web.MockCookie("refreshToken", refreshToken);
        mockMvc.perform(post("/auth/refresh-token")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token-123"))
                .andExpect(jsonPath("$.data.email").value("user@test.com"))
                .andExpect(jsonPath("$.data.userId").value(1L));

        verify(authenticationService, times(1)).refresh(refreshToken);
    }

    // ==================== POST /auth/log-out ====================
    @Test
    @DisplayName("POST /auth/log-out should logout successfully and clear cookie")
    void logout_withRefreshToken_shouldLogoutSuccessfully() throws Exception {
        String refreshToken = "refresh-token-123";

        doNothing().when(authenticationService).logout(refreshToken);

        org.springframework.mock.web.MockCookie cookie = new org.springframework.mock.web.MockCookie("refreshToken", refreshToken);
        mockMvc.perform(post("/auth/log-out")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Logout successful"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().maxAge("refreshToken", 0));

        verify(authenticationService, times(1)).logout(refreshToken);
    }

    @Test
    @DisplayName("POST /auth/log-out without refresh token should still logout")
    void logout_withoutRefreshToken_shouldStillLogout() throws Exception {
        doNothing().when(authenticationService).logout(null);

        mockMvc.perform(post("/auth/log-out"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Logout successful"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().maxAge("refreshToken", 0));

        verify(authenticationService, times(1)).logout(null);
    }

    // ==================== POST /auth/resend-verification ====================
    @Test
    @DisplayName("POST /auth/resend-verification should resend verification email")
    void resendVerification_shouldResendEmail() throws Exception {
        String email = "user@test.com";
        String name = "User Name";

        when(userService.getNameByAccountEmail(email)).thenReturn(name);
        doNothing().when(otpService).sendOtp(email, name);

        mockMvc.perform(post("/auth/resend-verification")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Verification email has been resent"))
                .andExpect(jsonPath("$.data").value(email));

        verify(userService, times(1)).getNameByAccountEmail(email);
        verify(otpService, times(1)).sendOtp(email, name);
    }

    // ==================== POST /auth/verify ====================
    @Test
    @DisplayName("POST /auth/verify should verify account successfully")
    void verifyAccount_shouldVerifySuccessfully() throws Exception {
        String code = "123456";
        String email = "user@test.com";

        doNothing().when(otpService).verifyOtp(code, email);

        mockMvc.perform(post("/auth/verify")
                        .param("code", code)
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Account verified successfully"));

        verify(otpService, times(1)).verifyOtp(code, email);
    }

    // ==================== POST /auth/register-email ====================
    @Test
    @DisplayName("POST /auth/register-email should register successfully")
    void registerEmail_shouldRegisterSuccessfully() throws Exception {
        EmailRegisterRequest request = new EmailRegisterRequest(
                "user@test.com",
                "User Name",
                java.time.LocalDate.of(1990, 1, 1),
                vn.cineshow.enums.Gender.MALE,
                "Hanoi",
                "password123",
                "password123",
                null
        );

        Long expectedId = 1L;
        when(authenticationService.registerByEmail(any(EmailRegisterRequest.class))).thenReturn(expectedId);

        mockMvc.perform(post("/auth/register-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Temp account created. Please verify OTP to activate."))
                .andExpect(jsonPath("$.data").value(expectedId));

        verify(authenticationService, times(1)).registerByEmail(any(EmailRegisterRequest.class));
    }

    @Test
    @DisplayName("POST /auth/register-email should return 400 when password mismatch")
    void registerEmail_passwordMismatch_shouldReturnBadRequest() throws Exception {
        EmailRegisterRequest request = new EmailRegisterRequest(
                "user@test.com",
                "User Name",
                java.time.LocalDate.of(1990, 1, 1),
                vn.cineshow.enums.Gender.MALE,
                "Hanoi",
                "password123",
                "password456", // Different password
                null
        );

        mockMvc.perform(post("/auth/register-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).registerByEmail(any(EmailRegisterRequest.class));
    }

    // ==================== POST /auth/verify-otp ====================
    @Test
    @DisplayName("POST /auth/verify-otp should verify OTP successfully")
    void verifyOtp_shouldVerifySuccessfully() throws Exception {
        String email = "user@test.com";
        String otp = "123456";

        doNothing().when(authenticationService).verifyAccountAndUpdateStatus(email, otp);

        mockMvc.perform(post("/auth/verify-otp")
                        .param("email", email)
                        .param("otp", otp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Account activated successfully"));

        verify(authenticationService, times(1)).verifyAccountAndUpdateStatus(email, otp);
    }

    // ==================== POST /auth/resend-otp ====================
    @Test
    @DisplayName("POST /auth/resend-otp should resend OTP successfully")
    void resendOtp_shouldResendSuccessfully() throws Exception {
        String email = "user@test.com";
        String name = "User Name";

        doNothing().when(otpService).sendOtp(email, name);

        mockMvc.perform(post("/auth/resend-otp")
                        .param("email", email)
                        .param("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Mã OTP mới đã được gửi đến email của bạn."));

        verify(otpService, times(1)).sendOtp(email, name);
    }

    // ==================== POST /auth/forgot-password ====================
    @Test
    @DisplayName("POST /auth/forgot-password should send OTP successfully")
    void forgotPassword_shouldSendOtpSuccessfully() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("user@test.com");

        when(accountService.forgotPassword(any(ForgotPasswordRequest.class))).thenReturn(true);

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("OTP sent to your email"));

        verify(accountService, times(1)).forgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    @DisplayName("POST /auth/forgot-password should return 400 when email not found")
    void forgotPassword_emailNotFound_shouldReturnBadRequest() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("notfound@test.com");

        when(accountService.forgotPassword(any(ForgotPasswordRequest.class))).thenReturn(false);

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email not found or OTP could not be sent"));

        verify(accountService, times(1)).forgotPassword(any(ForgotPasswordRequest.class));
    }

    // ==================== POST /auth/verify-otp-reset ====================
    @Test
    @DisplayName("POST /auth/verify-otp-reset should verify OTP and return token successfully")
    void verifyOtpReset_shouldReturnTokenSuccessfully() throws Exception {
        String email = "user@test.com";
        String otpCode = "123456";
        String token = "reset-token-123";

        OtpVerifyDTO request = new OtpVerifyDTO(email, otpCode);

        when(accountService.verifyOtpForReset(email, otpCode)).thenReturn(Optional.of(token));

        mockMvc.perform(post("/auth/verify-otp-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("OTP verified. Use this token to reset your password."))
                .andExpect(jsonPath("$.data.resetToken").value(token));

        verify(accountService, times(1)).verifyOtpForReset(email, otpCode);
    }

    @Test
    @DisplayName("POST /auth/verify-otp-reset should return 400 when OTP invalid")
    void verifyOtpReset_invalidOtp_shouldReturnBadRequest() throws Exception {
        String email = "user@test.com";
        String otpCode = "wrong-otp";

        OtpVerifyDTO request = new OtpVerifyDTO(email, otpCode);

        when(accountService.verifyOtpForReset(email, otpCode)).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/verify-otp-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid or expired OTP"));

        verify(accountService, times(1)).verifyOtpForReset(email, otpCode);
    }

    // ==================== POST /auth/reset-password ====================
    @Test
    @DisplayName("POST /auth/reset-password should reset password successfully")
    void resetPassword_shouldResetSuccessfully() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setResetToken("reset-token-123");
        request.setNewPassword("newPassword123");

        when(accountService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(true);

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Password reset successfully"));

        verify(accountService, times(1)).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("POST /auth/reset-password should return 400 when token invalid")
    void resetPassword_invalidToken_shouldReturnBadRequest() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setResetToken("invalid-token");
        request.setNewPassword("newPassword123");

        when(accountService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(false);

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid OTP or expired"));

        verify(accountService, times(1)).resetPassword(any(ResetPasswordRequest.class));
    }
}
