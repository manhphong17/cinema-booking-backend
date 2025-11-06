package vn.cineshow.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.dto.request.ResetPasswordRequest;
import vn.cineshow.dto.request.SignInRequest;
import vn.cineshow.dto.response.SignInResponse;
import vn.cineshow.dto.response.TokenResponse;
import vn.cineshow.enums.AccountStatus;
import vn.cineshow.exception.*;
import vn.cineshow.model.*;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.PasswordResetTokenRepository;
import vn.cineshow.repository.RefreshTokenRepository;
import vn.cineshow.repository.RoleRepository;
import vn.cineshow.service.AuthenticationService;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.OtpService;
import vn.cineshow.service.RefreshTokenService;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-SERVICE-IMPL")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    AccountRepository accountRepository;
    JWTService jwtService;
    AuthenticationManager authenticationManager;
    RefreshTokenRepository refreshTokenRepository;
    RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;
    private final OtpService otpService;

    private static final SecureRandom RNG = new SecureRandom();

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    private static String base64Url(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }


    @Override
    @Transactional
    public TokenResponse signIn(SignInRequest req) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Email or Password invalid");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Account account = accountRepository.findAccountByEmail(req.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));

        String accessToken = jwtService.generateAccessToken(req.getEmail(), authorities, account.getId());
        String refreshToken = jwtService.generateRefreshToken(req.getEmail(), authorities);
        log.info("refresh token: " + refreshToken);

        refreshTokenService.replaceRefreshToken(account, refreshToken, jwtService.getRefreshTokenExpiryInSecond());

        List<String> roleNames = account.getRoles().stream()
                .map(Role::getRoleName)
                .toList();

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(account.getId())
                .roleNames(roleNames)
                .email(account.getEmail())
                .build();
    }

    @Override
    @Transactional
    public SignInResponse refresh(String refreshToken) {
        RefreshToken entity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthenticationServiceException("Invalid refresh token"));

        if (entity.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(entity);
            throw new AuthenticationServiceException("Refresh token expired");
        }

        Account account = entity.getAccount();

        List<String> roleNames = account.getRoles().stream()
                .map(Role::getRoleName)
                .toList();

        String newAccessToken = jwtService.generateAccessToken(
                account.getEmail(),
                roleNames,
                account.getId());

        return SignInResponse.builder()
                .accessToken(newAccessToken)
                .userId(account.getId())
                .roleName(roleNames)
                .email(account.getEmail())
                .build();
    }


    private boolean isAccountExists(String email) {
        return accountRepository.findByEmail(email).isPresent();
    }


    //==============================================
    //==================== REGISTER================


    @Transactional
    @Override
    public long registerByEmail(EmailRegisterRequest req) {

        Optional<Account> accountOtp = accountRepository.findAccountByEmail(req.email());

        // TH1: Đã có account
        if (accountOtp.isPresent()) {
            Account account = accountOtp.get();

            if (account.getStatus().equals(AccountStatus.ACTIVE)) {
                throw new DuplicateResourceException("Email already exists");
            } else if (account.getStatus().equals(AccountStatus.DEACTIVATED)) {
                throw new AuthenticatedException("Your account has been locked by administrator");
            }

            // Nếu đang PENDING → cập nhật lại user info + password, gửi OTP mới
            User user = account.getUser();
            if (user == null) {
                // Tạo mới user nếu DB đang bị thiếu (dirty data)
                user = new User();
                user.setAccount(account); // rất quan trọng: set ngược lại để Hibernate map quan hệ
            }
            user.setName(req.name());
            user.setDateOfBirth(req.dateOfBirth());
            user.setGender(req.gender());
            if (req.address() != null && !req.address().isBlank()) {
                user.setAddress(req.address().trim());
            }

            account.setUser(user);
            account.setPassword(encoder.encode(req.password()));
            account.setStatus(AccountStatus.PENDING); // reset lại trạng thái pending nếu cần
            accountRepository.save(account);

            otpService.sendOtp(req.email(), req.name());
            return account.getId();
        }

        // TH2: Chưa có account → tạo mới
        Role customer = roleRepo.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role CUSTOMER not found"));

        User user = User.builder()
                .name(req.name())
                .dateOfBirth(req.dateOfBirth())
                .gender(req.gender())
                .address(req.address() != null ? req.address() : null)
                .build();


        Account account = Account.builder()
                .email(req.email())
                .password(encoder.encode(req.password()))
                .status(AccountStatus.PENDING)
                .roles(Set.of(customer))
                .user(user)
                .build();

        // nhớ set ngược lại
        user.setAccount(account);

        accountRepository.save(account);

        otpService.sendOtp(req.email(), req.name());
        return account.getId();
    }


    @Transactional
    @Override
    public void verifyAccountAndUpdateStatus(String email, String otp) {
        // Gọi lại hàm verifyOtp() trong otpService
        boolean verified = otpService.verifyOtp(email, otp);

        if (verified) {
            Account account = accountRepository.findAccountByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

            //  Cập nhật trạng thái tài khoản
            account.setStatus(AccountStatus.ACTIVE);
            accountRepository.save(account);
        }
    }

    @Override
    @Transactional
    public boolean forgotPassword(ForgotPasswordRequest request) {
        // 400: invalid input
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }
        final String email = request.getEmail().trim();

        // 404: Find accoung or throw exception if not found
        Optional<Account> accOpt = accountRepository.findAccountByEmail(email);
        if (accOpt.isEmpty()) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
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
            throw new AppException(ErrorCode.OTP_SEND_FAILED);
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
            throw new AppException(ErrorCode.OTP_INVALID);
        }

        // 2) tạo verifier & hash (DB chỉ lưu hash)
        byte[] random = new byte[48];
        RNG.nextBytes(random);
        // It's good practice to clean the array after use if it contains sensitive data,
        //through for a random verifier, it's less critical than for passwords
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
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        final String resetToken = request.getResetToken().trim();
        final String newPassword = request.getNewPassword();

        // 1) Check format "<tokenId>.<verifier>"
        //A more robust check might use regex, but this is functional for the expected format.
        final int dot = resetToken.indexOf('.');
        if (dot <= 0 || dot == resetToken.length() - 1) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }
        final String tokenId = resetToken.substring(0, dot);
        final String verifier = resetToken.substring(dot + 1);

        // 2) Load token
        Optional<PasswordResetToken> opt = passwordResetTokenRepository.findById(tokenId);
        if (opt.isEmpty()) {
            throw new AppException(ErrorCode.PASSWORD_RESET_TOKEN_NOT_FOUND);
        }
        PasswordResetToken prt = opt.get();// safe to get now due to isEmty check

        // 3) Validate trạng thái token
        if (prt.isUsed() || prt.getExpiresAt() == null || prt.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }

        // 4) Verify verifier với hash trong DB
        boolean matches;
        try {
            matches = passwordEncoder.matches(verifier, prt.getTokenHash());
        } catch (Exception ex) {//Catching generic Exception is broad, consider spesific crypto exceptions if any.
            matches = false;
        }
        if (!matches) {
            throw new AppException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }

        // 5) Consume token (one-time)
        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);

        // 6) Cập nhật mật khẩu theo email gắn với token
        Optional<Account> accOpt = accountRepository.findAccountByEmail(prt.getEmail());
        if (!accOpt.isPresent()) {
            // 404 trung tính (để GlobalExceptionHandler map về VN, không lộ email)
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        Account acc = accOpt.get();
        acc.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(acc);

        //  session/refresh if needed
        return true;
    }

}