package vn.cineshow.service;


import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.dto.request.ResetPasswordRequest;

import java.util.Optional;

public interface AccountService {
    long createCustomerAccount(EmailRegisterRequest req);

    boolean forgotPassword(ForgotPasswordRequest request);

    // return Optional<String> so controller can return resetToken in body
    Optional<String> verifyOtpForReset(String email, String otp);

    boolean resetPassword(ResetPasswordRequest request);

}
