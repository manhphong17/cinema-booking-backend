package vn.cineshow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import vn.cineshow.dto.response.VerifyOtpResetResponse;
import vn.cineshow.service.AccountService;
import vn.cineshow.service.AuthenticationService;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.OtpService;
import vn.cineshow.service.impl.AccountDetailsService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private OtpService otpService;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AccountDetailsService accountDetailsService;

    // ==================== POST /accounts/register-email ====================
    @Test
    @DisplayName("POST /accounts/register-email should register successfully")
    void registerEmail_shouldRegisterSuccessfully() throws Exception {
        EmailRegisterRequest request = new EmailRegisterRequest(
                "user@example.com",
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

        mockMvc.perform(post("/accounts/register-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Temp account created. Please verify OTP to activate."))
                .andExpect(jsonPath("$.data").value(expectedId));

        ArgumentCaptor<EmailRegisterRequest> captor = ArgumentCaptor.forClass(EmailRegisterRequest.class);
        verify(authenticationService, times(1)).registerByEmail(captor.capture());
        assertThat(captor.getValue().email()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("POST /accounts/register-email should return 400 when password mismatch")
    void registerEmail_passwordMismatch_shouldReturnBadRequest() throws Exception {
        EmailRegisterRequest request = new EmailRegisterRequest(
                "user@example.com",
                "User Name",
                java.time.LocalDate.of(1990, 1, 1),
                vn.cineshow.enums.Gender.MALE,
                "Hanoi",
                "password123",
                "password456", // Different password
                null
        );

        mockMvc.perform(post("/accounts/register-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).registerByEmail(any());
    }

    // ==================== POST /accounts/verify-otp ====================
    @Test
    @DisplayName("POST /accounts/verify-otp should verify OTP successfully")
    void verifyOtp_shouldVerifySuccessfully() throws Exception {
        String email = "user@example.com";
        String otp = "123456";

        doNothing().when(authenticationService).verifyAccountAndUpdateStatus(email, otp);

        mockMvc.perform(post("/accounts/verify-otp")
                        .param("email", email)
                        .param("otp", otp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Account activated successfully"));

        verify(authenticationService, times(1)).verifyAccountAndUpdateStatus(email, otp);
    }

    // ==================== POST /accounts/resend-otp ====================
    @Test
    @DisplayName("POST /accounts/resend-otp should resend OTP successfully")
    void resendOtp_shouldResendSuccessfully() throws Exception {
        String email = "user@example.com";
        String name = "User Name";

        doNothing().when(otpService).sendOtp(email, name);

        mockMvc.perform(post("/accounts/resend-otp")
                        .param("email", email)
                        .param("name", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Mã OTP mới đã được gửi đến email của bạn."));

        verify(otpService, times(1)).sendOtp(email, name);
    }

    // ==================== POST /accounts/forgot-password ====================
    @Test
    @DisplayName("POST /accounts/forgot-password should send OTP successfully")
    void forgotPassword_shouldSendOtpSuccessfully() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("user@example.com");

        when(accountService.forgotPassword(any(ForgotPasswordRequest.class))).thenReturn(true);

        mockMvc.perform(post("/accounts/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("OTP sent to your email"));

        verify(accountService, times(1)).forgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    @DisplayName("POST /accounts/forgot-password should return 400 when email not found")
    void forgotPassword_emailNotFound_shouldReturnBadRequest() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("notfound@example.com");

        when(accountService.forgotPassword(any(ForgotPasswordRequest.class))).thenReturn(false);

        mockMvc.perform(post("/accounts/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email not found or OTP could not be sent"));

        verify(accountService, times(1)).forgotPassword(any(ForgotPasswordRequest.class));
    }

    // ==================== POST /accounts/verify-otp-reset ====================
    @Test
    @DisplayName("POST /accounts/verify-otp-reset should verify OTP and return token successfully")
    void verifyOtpReset_shouldReturnTokenSuccessfully() throws Exception {
        String email = "user@example.com";
        String otpCode = "123456";
        String token = "reset-token-123";

        OtpVerifyDTO request = new OtpVerifyDTO(email, otpCode);

        when(accountService.verifyOtpForReset(email, otpCode)).thenReturn(Optional.of(token));

        mockMvc.perform(post("/accounts/verify-otp-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("OTP verified. Use this token to reset your password."))
                .andExpect(jsonPath("$.data.resetToken").value(token));

        verify(accountService, times(1)).verifyOtpForReset(email, otpCode);
    }

    @Test
    @DisplayName("POST /accounts/verify-otp-reset should return 400 when OTP invalid")
    void verifyOtpReset_invalidOtp_shouldReturnBadRequest() throws Exception {
        String email = "user@example.com";
        String otpCode = "wrong-otp";

        OtpVerifyDTO request = new OtpVerifyDTO(email, otpCode);

        when(accountService.verifyOtpForReset(email, otpCode)).thenReturn(Optional.empty());

        mockMvc.perform(post("/accounts/verify-otp-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid or expired OTP"));

        verify(accountService, times(1)).verifyOtpForReset(email, otpCode);
    }

    // ==================== POST /accounts/reset-password ====================
    @Test
    @DisplayName("POST /accounts/reset-password should reset password successfully")
    void resetPassword_shouldResetSuccessfully() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setResetToken("reset-token-123");
        request.setNewPassword("newPassword123");

        when(accountService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(true);

        mockMvc.perform(post("/accounts/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Password reset successfully"));

        verify(accountService, times(1)).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    @DisplayName("POST /accounts/reset-password should return 400 when token invalid")
    void resetPassword_invalidToken_shouldReturnBadRequest() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setResetToken("invalid-token");
        request.setNewPassword("newPassword123");

        when(accountService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(false);

        mockMvc.perform(post("/accounts/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid OTP or expired"));

        verify(accountService, times(1)).resetPassword(any(ResetPasswordRequest.class));
    }
}

