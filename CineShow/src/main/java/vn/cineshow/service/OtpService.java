package vn.cineshow.service;


public interface OtpService {
    void sendOtp(String email, String name);             // tạo + gửi (có cooldown)


    boolean verifyOtp(String email, String otp);
}