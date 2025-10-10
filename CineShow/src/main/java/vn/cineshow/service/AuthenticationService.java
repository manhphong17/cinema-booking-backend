package vn.cineshow.service;


import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.SignInRequest;
import vn.cineshow.dto.response.SignInResponse;
import vn.cineshow.dto.response.TokenResponse;

public interface AuthenticationService {

    TokenResponse signIn(SignInRequest req);

    SignInResponse refresh(String refreshToken);

    long registerByEmail(EmailRegisterRequest req);

    void verifyAccountAndUpdateStatus(String email, String otp);
}