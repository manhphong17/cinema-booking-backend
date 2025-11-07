package vn.cineshow.service.impl;

import com.sendgrid.SendGrid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.OtpCode;
import vn.cineshow.repository.OtpCodeRepository;
import vn.cineshow.service.OtpService;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "OTP-SERVICE")
public class OtpServiceImpl implements OtpService {

    // =====  POLICY =====
    private static final Duration EXPIRES_IN = Duration.ofMinutes(4);
    private static final SecureRandom RNG = new SecureRandom();
    private final OtpCodeRepository otpRepo;
    private final EmailServiceImpl emailService;
    private final PasswordEncoder passwordEncoder;
    private final SendGrid sendgrid;
    @Value("${spring.sendgrid.from-email}")
    private String fromEmail;

    private static String generateOtp(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(RNG.nextInt(10));
        return sb.toString();
    }

    @Transactional
    @Override
    public void sendOtp(String email, String name) {
        var now = Instant.now();
        log.info("Time " + now);
        var code = otpRepo.findByEmail(email).orElse(null);

        String otp = generateOtp(6);
        String hash = passwordEncoder.encode(otp);

        if (code == null) {
            code = OtpCode.builder()
                    .email(email)
                    .otpHash(hash)
                    .expiresAt(now.plus(EXPIRES_IN))
                    .used(false)
                    .build();
        } else {
            code.setOtpHash(hash);
            code.setExpiresAt(now.plus(EXPIRES_IN));
            code.setUsed(false);
        }

        otpRepo.save(code);

        // gửi email (nên async nếu load cao)
//        emailService.sendOtpEmail(email, name, otp);
        emailService.send(email, "Your OTP code", "Hello " + name + ". Your OTP is: " + otp);
    }


    @Transactional
    @Override
    public boolean verifyOtp(String email, String otp) {
        var code = otpRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_FOUND));

        if (code.isUsed() || code.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        if (!passwordEncoder.matches(otp, code.getOtpHash())) {
            throw new AppException(ErrorCode.OTP_INVALID);
        }

        code.setUsed(true);
        otpRepo.save(code);
        return true;
    }

}
