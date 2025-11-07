package vn.cineshow.service;


import vn.cineshow.dto.request.ChangePasswordRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.dto.request.ResetPasswordRequest;

import java.util.Optional;

public interface AccountService {

    boolean forgotPassword(ForgotPasswordRequest request);

    // return Optional<String> so controller can return resetToken in body
    Optional<String> verifyOtpForReset(String email, String otp);

    boolean resetPassword(ResetPasswordRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

}
