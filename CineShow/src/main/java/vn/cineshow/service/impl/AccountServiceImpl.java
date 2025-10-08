package vn.cineshow.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.dto.request.ResetPasswordRequest;
import vn.cineshow.enums.AccountStatus;
import vn.cineshow.enums.AuthProvider;
import vn.cineshow.model.*;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.OtpCodeRepository;
import vn.cineshow.repository.PasswordResetTokenRepository;
import vn.cineshow.repository.RefreshTokenRepository;
import vn.cineshow.service.AccountService;
import vn.cineshow.service.OtpService;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "OTP-SERVICE")
class AccountServiceImpl implements AccountService {


    private static final SecureRandom RNG = new SecureRandom();

    private final AccountRepository accountRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository; // keep if you have it
    private final OtpService otpService; // delegate all OTP logic here

    private static String base64Url(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    @Override
    public long createCustomerAccount(EmailRegisterRequest req) {
        User user = User.builder()
                .name(req.name())
                .gender(req.gender())
                .dateOfBirth(req.dateOfBirth()).build();


        Account account = new Account();
        Account.builder()
                .email(req.email())
                .password(req.password())
                .status(AccountStatus.PENDING)
                .user(user)
                .build();

        AccountProvider provider = AccountProvider.builder()
                .account(account)
                .provider(AuthProvider.LOCAL)
                .build();

        account.setProviders(List.of(provider));

        accountRepository.save(account);


        return account.getId();
    }

    // -----------------------------------------------------------------------------------
    // Forgot password: validate → check email tồn tại → send OTP
    // 404 user-not-found: ném UsernameNotFoundException (handler sẽ trả thông điệp VN trung tính)
    // -----------------------------------------------------------------------------------
    @Override
    @Transactional
    public boolean forgotPassword(ForgotPasswordRequest request) {
        // 400: invalid input
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }
        final String email = request.getEmail().trim();

        // 404: dùng UsernameNotFoundException để GlobalExceptionHandler xử lý, không lộ email
        Optional<Account> accOpt = accountRepository.findAccountByEmail(email);
        if (!accOpt.isPresent()) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng");
        }

        // Lấy tên hiển thị nếu có (không quan trọng, chỉ để email content)
        String name = "bạn";
        try {
            Account acc = accOpt.get();
            if (acc.getUser() != null && acc.getUser().getName() != null && !acc.getUser().getName().isBlank()) {
                name = acc.getUser().getName();
            }
        } catch (Exception ignore) {
            // keep neutral name
        }

        // Gửi OTP hoặc 500
        try {
            otpService.sendOtp(email, name);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send OTP to {}", email, ex);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Gửi OTP thất bại");
        }
    }

    // -----------------------------------------------------------------------------------
    // Verify OTP và phát hành resetToken (trả về cho Controller → FE nhận trong body)
    // -----------------------------------------------------------------------------------
    @Override
    @Transactional
    public Optional<String> verifyOtpForReset(String email, String otpInput) {
        // 1) verify OTP (plain vs hashed) → 400 nếu không hợp lệ
        boolean verified;
        try {
            verified = otpService.verifyOtp(email, otpInput);
        } catch (Exception ex) {
            verified = false;
        }
        if (!verified) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }

        // 2) tạo verifier & hash (DB chỉ lưu hash)
        byte[] random = new byte[48];
        RNG.nextBytes(random);
        String verifier = base64Url(random);
        String tokenHash = passwordEncoder.encode(verifier);

        // 3) upsert: mỗi email 1 bản ghi
        PasswordResetToken prt = passwordResetTokenRepository.findByEmail(email).orElse(null);
        if (prt == null) {
            prt = new PasswordResetToken();
            prt.setEmail(email);
        }
        prt.setUsed(false);
        prt.setExpiresAt(Instant.now().plusSeconds(20 * 60)); // 20 phút
        prt.setTokenHash(tokenHash);

        // 4) lưu và trả token công khai "<id>.<verifier>"
        prt = passwordResetTokenRepository.save(prt);
        String resetToken = prt.getId() + "." + verifier;
        return Optional.of(resetToken);
    }

    // -----------------------------------------------------------------------------------
    // Reset password bằng resetToken ("<tokenId>.<verifier>")
    // -----------------------------------------------------------------------------------
    @Override
    @Transactional
    public boolean resetPassword(ResetPasswordRequest request) {
        // 0) Validate payload
        if (request == null ||
                request.getResetToken() == null || request.getResetToken().isBlank() ||
                request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }

        final String resetToken = request.getResetToken().trim();
        final String newPassword = request.getNewPassword();

        // 1) Check format "<tokenId>.<verifier>"
        final int dot = resetToken.indexOf('.');
        if (dot <= 0 || dot == resetToken.length() - 1) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }
        final String tokenId = resetToken.substring(0, dot);
        final String verifier = resetToken.substring(dot + 1);

        // 2) Load token
        Optional<PasswordResetToken> opt = passwordResetTokenRepository.findById(tokenId);
        if (!opt.isPresent()) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }
        PasswordResetToken prt = opt.get();

        // 3) Validate trạng thái token
        if (prt.isUsed() || prt.getExpiresAt() == null || prt.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }

        // 4) Verify verifier với hash trong DB
        boolean matches;
        try {
            matches = passwordEncoder.matches(verifier, prt.getTokenHash());
        } catch (Exception ex) {
            matches = false;
        }
        if (!matches) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }

        // 5) Consume token (one-time)
        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);

        // 6) Cập nhật mật khẩu theo email gắn với token
        Optional<Account> accOpt = accountRepository.findAccountByEmail(prt.getEmail());
        if (!accOpt.isPresent()) {
            // 404 trung tính (để GlobalExceptionHandler map về VN, không lộ email)
            throw new UsernameNotFoundException("Không tìm thấy người dùng");
        }

        Account acc = accOpt.get();
        acc.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(acc);

        // (tùy chọn) Thu hồi session/refresh tại đây nếu bạn hỗ trợ
        return true;
    }
}
