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
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.SignInRequest;
import vn.cineshow.dto.response.SignInResponse;
import vn.cineshow.dto.response.TokenResponse;
import vn.cineshow.enums.AccountStatus;
import vn.cineshow.exception.*;
import vn.cineshow.model.Account;
import vn.cineshow.model.RefreshToken;
import vn.cineshow.model.Role;
import vn.cineshow.model.User;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.RefreshTokenRepository;
import vn.cineshow.repository.RoleRepository;
import vn.cineshow.service.AuthenticationService;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.OtpService;
import vn.cineshow.service.RefreshTokenService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

        String accessToken = jwtService.generateAccessToken(req.getEmail(), authorities);
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
                roleNames);

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

}