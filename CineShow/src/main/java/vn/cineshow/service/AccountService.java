package vn.cineshow.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.cineshow.dto.request.ChangePasswordRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.dto.request.ResetPasswordRequest;
import vn.cineshow.dto.request.account.AccountChangePasswordRequest;
import vn.cineshow.dto.request.account.AccountCreateRequest;
import vn.cineshow.dto.request.account.AccountUpdateRequest;
import vn.cineshow.dto.response.account.AccountResponse;

import java.util.Optional;

public interface AccountService {

    boolean forgotPassword(ForgotPasswordRequest request);

    // return Optional<String> so controller can return resetToken in body
    Optional<String> verifyOtpForReset(String email, String otp);

    boolean resetPassword(ResetPasswordRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);


    void adminChangePassword(Long userId, AccountChangePasswordRequest request); // 👈 THÊM



    // ========= CRUD =========
    AccountResponse create(AccountCreateRequest req);

    AccountResponse update(Long id, AccountUpdateRequest req);

    Page<AccountResponse> list(Pageable pageable);


    void restore(Long id); // set isDeleted = false



}
