package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;

import vn.cineshow.dto.request.ChangePasswordRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.dto.request.ResetPasswordRequest;
import vn.cineshow.dto.request.account.AccountChangePasswordRequest;
import vn.cineshow.dto.request.account.AccountCreateRequest;
import vn.cineshow.dto.request.account.AccountUpdateRequest;
import vn.cineshow.dto.response.account.AccountResponse;
import vn.cineshow.dto.response.account.RoleItemResponse;
import vn.cineshow.enums.AccountStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.Account;
import vn.cineshow.model.PasswordResetToken;
import vn.cineshow.model.User;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.PasswordResetTokenRepository;
import vn.cineshow.repository.RefreshTokenRepository;
import vn.cineshow.repository.RoleRepository;
import vn.cineshow.service.AccountService;
import vn.cineshow.service.OtpService;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "OTP-SERVICE")
public class AccountServiceImpl implements AccountService {

    private static final SecureRandom RNG = new SecureRandom();

    private final AccountRepository accountRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository; // (chưa dùng vẫn giữ)
    private final OtpService otpService;
    private final RoleRepository roleRepository;

    // ========================= Helpers =========================

    private static String base64Url(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    /**
     * Validate password strength:
     * - >= 8 ký tự
     * - có chữ hoa
     * - có chữ thường
     * - có số
     */
    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new AppException(ErrorCode.PASSWORD_TOO_WEAK);
        }

        boolean hasUpperCase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowerCase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasUpperCase || !hasLowerCase || !hasDigit) {
            throw new AppException(ErrorCode.PASSWORD_TOO_WEAK);
        }
    }

    // ==================== Forgot password (OTP) ====================

    @Override
    @Transactional
    public boolean forgotPassword(ForgotPasswordRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }
        final String email = request.getEmail().trim();

        Optional<Account> accOpt = accountRepository.findAccountByEmail(email);
        if (!accOpt.isPresent()) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng");
        }

        String name = "bạn";
        try {
            Account acc = accOpt.get();
            if (acc.getUser() != null &&
                    acc.getUser().getName() != null &&
                    !acc.getUser().getName().isBlank()) {
                name = acc.getUser().getName();
            }
        } catch (Exception ignore) {
        }

        try {
            otpService.sendOtp(email, name);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send OTP to {}", email, ex);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Gửi OTP thất bại");
        }
    }

    // ==================== Verify OTP + phát hành reset token ====================

    @Override
    @Transactional
    public Optional<String> verifyOtpForReset(String email, String otpInput) {
        boolean verified;
        try {
            verified = otpService.verifyOtp(email, otpInput);
        } catch (Exception ex) {
            verified = false;
        }
        if (!verified) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }

        byte[] random = new byte[48];
        RNG.nextBytes(random);
        String verifier = base64Url(random);
        String tokenHash = passwordEncoder.encode(verifier);

        PasswordResetToken prt = passwordResetTokenRepository.findByEmail(email).orElse(null);
        if (prt == null) {
            prt = new PasswordResetToken();
            prt.setEmail(email);
        }
        prt.setUsed(false);
        prt.setExpiresAt(Instant.now().plusSeconds(20 * 60)); // 20 phút
        prt.setTokenHash(tokenHash);

        prt = passwordResetTokenRepository.save(prt);
        String resetToken = prt.getId() + "." + verifier;
        return Optional.of(resetToken);
    }

    // ==================== Reset password bằng resetToken ====================

    @Override
    @Transactional
    public boolean resetPassword(ResetPasswordRequest request) {
        if (request == null ||
                request.getResetToken() == null || request.getResetToken().isBlank() ||
                request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }

        final String resetToken = request.getResetToken().trim();
        final String newPassword = request.getNewPassword();

        final int dot = resetToken.indexOf('.');
        if (dot <= 0 || dot == resetToken.length() - 1) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }
        final String tokenId = resetToken.substring(0, dot);
        final String verifier = resetToken.substring(dot + 1);

        Optional<PasswordResetToken> opt = passwordResetTokenRepository.findById(tokenId);
        if (!opt.isPresent()) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }
        PasswordResetToken prt = opt.get();

        if (prt.isUsed() ||
                prt.getExpiresAt() == null ||
                prt.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }

        boolean matches;
        try {
            matches = passwordEncoder.matches(verifier, prt.getTokenHash());
        } catch (Exception ex) {
            matches = false;
        }
        if (!matches) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }

        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);

        Optional<Account> accOpt = accountRepository.findAccountByEmail(prt.getEmail());
        if (!accOpt.isPresent()) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng");
        }

        Account acc = accOpt.get();
        acc.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(acc);

        return true;
    }

    // ==================== Change password cho user đã đăng nhập ====================

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (request == null ||
                request.getCurrentPassword() == null || request.getCurrentPassword().isBlank() ||
                request.getNewPassword() == null || request.getNewPassword().isBlank() ||
                request.getConfirmPassword() == null || request.getConfirmPassword().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(BAD_REQUEST, "Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new ResponseStatusException(BAD_REQUEST, "Mật khẩu mới không được trùng với mật khẩu hiện tại");
        }

        Optional<Account> accOpt = accountRepository.findById(userId);
        if (!accOpt.isPresent()) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng");
        }

        Account account = accOpt.get();

        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())) {
            throw new ResponseStatusException(BAD_REQUEST, "Mật khẩu hiện tại không đúng");
        }

        validatePasswordStrength(request.getNewPassword());

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        log.info("Password changed successfully for user: {}", userId);
    }
// ============================== CRUD ==============================

    @Override
    @Transactional
    public AccountResponse create(AccountCreateRequest req) {
        if (req == null ||
                req.getName() == null || req.getName().isBlank() ||
                req.getEmail() == null || req.getEmail().isBlank() ||
                req.getPassword() == null || req.getPassword().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }

        if (req.getRoleIds() == null || req.getRoleIds().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Phải chọn ít nhất 1 vai trò");
        }

        final String email = req.getEmail().trim().toLowerCase();

        // Chặn trùng email (kể cả đã xoá mềm)
        if (accountRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(BAD_REQUEST, "Email đã tồn tại");
        }

        // (tuỳ chọn) kiểm tra độ mạnh mật khẩu
        // validatePasswordStrength(req.getPassword());

        // Lấy roles theo roleIds
        Set<vn.cineshow.model.Role> roles = roleRepository.findAllById(req.getRoleIds())
                .stream()
                .collect(Collectors.toSet());
        if (roles.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Vai trò không hợp lệ");
        }

        // 1) Tạo Account trước
        Account acc = Account.builder()
                .email(email)
                .password(passwordEncoder.encode(req.getPassword()))
                .status(AccountStatus.ACTIVE)
                .roles(roles)
                .build();

        // 2) Tạo User gắn với Account (lưu tên vào bảng users)
        User user = new User();
        user.setName(req.getName());
        user.setAccount(acc);  // @MapsId
        acc.setUser(user);     // nếu phía Account có mappedBy + cascade ALL thì save(acc) sẽ save luôn user

        // 3) Lưu
        acc = accountRepository.save(acc);

        return toResponse(acc);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> list(org.springframework.data.domain.Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public AccountResponse update(Long id, AccountUpdateRequest req) {
        if (id == null || req == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }

        Account acc = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Không tìm thấy người dùng"));

        // Cập nhật tên -> lưu ở bảng users
        if (req.getName() != null && !req.getName().isBlank()) {
            User user = acc.getUser();
            if (user == null) {
                user = new User();
                user.setAccount(acc);   // @MapsId: id user = id account
                acc.setUser(user);
            }
            user.setName(req.getName());
        }

        // Soft delete / restore
        if (req.getDeleted() != null) {
            acc.setDeleted(Boolean.TRUE.equals(req.getDeleted()));
        }

        // Status (AccountStatus enum)
        if (req.getStatus() != null) {
            acc.setStatus(req.getStatus());
        }

        // Roles: null -> bỏ qua, [] -> clear hết
        if (req.getRoleIds() != null) {
            acc.setRoles(
                    roleRepository.findAllById(req.getRoleIds())
                            .stream()
                            .collect(Collectors.toSet())
            );
        }

        // Đổi mật khẩu nếu có
        if (req.getNewPassword() != null && !req.getNewPassword().isBlank()) {
            validatePasswordStrength(req.getNewPassword());
            acc.setPassword(passwordEncoder.encode(req.getNewPassword()));
        }

        acc = accountRepository.save(acc);
        return toResponse(acc);
    }

// ============================ Helper mapper ============================

    private AccountResponse toResponse(Account a) {
        Set<RoleItemResponse> roleDtos;

        if (a.getRoles() == null || a.getRoles().isEmpty()) {
            roleDtos = Set.of();
        } else {
            roleDtos = a.getRoles().stream()
                    .map(r -> RoleItemResponse.builder()
                            .id(r.getId())
                            .name(r.getRoleName())
                            .build())
                    .collect(Collectors.toSet());
        }

        return AccountResponse.builder()
                .id(a.getId())
                .email(a.getEmail())
                .status(a.getStatus())
                .deleted(a.isDeleted())
                .roles(roleDtos)
                // 👉 trả về tên từ bảng users (nếu cần cho FE)
                .name(a.getUser() != null ? a.getUser().getName() : null)
                .createdAt(
                        a.getCreatedAt() == null
                                ? null
                                : a.getCreatedAt()
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                )
                .build();
    }


    // ==================== Admin đổi mật khẩu trực tiếp ====================
    @Override
    @Transactional
    public void adminChangePassword(Long userId, AccountChangePasswordRequest request) {
        if (request == null ||
                request.getNewPassword() == null || request.getNewPassword().isBlank() ||
                request.getConfirmPassword() == null || request.getConfirmPassword().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Tham số không hợp lệ");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(BAD_REQUEST, "Mật khẩu mới và xác nhận mật khẩu không khớp");
        }


        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Không tìm thấy người dùng"));

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
        log.info("Admin changed password for user: {}", userId);
    }


    @Override
    @Transactional
    public void restore(Long id) {
        Account acc = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Không tìm thấy người dùng"));
        acc.setDeleted(false);
        accountRepository.save(acc);
    }
}