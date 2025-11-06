package vn.cineshow.service;


import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.dto.request.ResetPasswordRequest;
import vn.cineshow.dto.request.SignInRequest;
import vn.cineshow.dto.response.SignInResponse;
import vn.cineshow.dto.response.TokenResponse;

import java.util.Optional;

public interface AuthenticationService {

    TokenResponse signIn(SignInRequest req);

    SignInResponse refresh(String refreshToken);

    long registerByEmail(EmailRegisterRequest req);

    void verifyAccountAndUpdateStatus(String email, String otp);

    boolean forgotPassword(ForgotPasswordRequest request);

    // return Optional<String> so controller can return resetToken in body
    Optional<String> verifyOtpForReset(String email, String otp);

    boolean resetPassword(ResetPasswordRequest request);


}