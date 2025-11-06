package vn.cineshow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.cineshow.dto.request.UpdateUserRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.UserResponse;
import vn.cineshow.exception.AuthenticatedException;
import vn.cineshow.service.UserService;
import vn.cineshow.service.impl.S3Service;

import java.io.IOException;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final S3Service s3Service;

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('CUSTOMER')")
    public ResponseData<UserResponse> getProfile(@AuthenticationPrincipal UserDetails principal) {
        System.out.println("=== USER CONTROLLER DEBUG ===");
        System.out.println("Principal: " + principal);
        System.out.println("Principal class: " + (principal != null ? principal.getClass().getSimpleName() : "null"));
        System.out.println("Principal username: " + (principal != null ? principal.getUsername() : "null"));
        System.out.println("Principal authorities: " + (principal != null ? principal.getAuthorities() : "null"));

        String email = resolveEmail(principal);

        System.out.println("Resolved email: " + email);
        System.out.println("=== END USER CONTROLLER DEBUG ===");

        UserResponse response = userService.getProfile(email);
        return new ResponseData<>(HttpStatus.OK.value(), "Fetched user profile successfully", response);
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyAuthority('CUSTOMER','ADMIN','BUSINESS','OPERATION')")
    public ResponseData<UserResponse> updateProfile(@AuthenticationPrincipal UserDetails principal,
                                                    @RequestBody @Valid UpdateUserRequest request) {
        UserResponse response = userService.updateProfile(resolveEmail(principal), request);
        return new ResponseData<>(HttpStatus.OK.value(), "Updated user profile successfully", response);
    }

    @PostMapping("/me/avatar")
    @PreAuthorize("hasAnyAuthority('CUSTOMER','ADMIN','BUSINESS','OPERATION')")
    public ResponseData<String> uploadAvatar(@AuthenticationPrincipal UserDetails principal,
                                             @RequestParam("file") MultipartFile file) throws IOException {
        // Upload file lên S3
        String avatarUrl = s3Service.uploadFile(file);

        // Cập nhật avatar URL vào user profile
        UpdateUserRequest request = UpdateUserRequest.builder()
                .avatar(avatarUrl)
                .build();
        userService.updateProfile(resolveEmail(principal), request);

        return new ResponseData<>(HttpStatus.OK.value(), "Avatar uploaded successfully", avatarUrl);
    }

    private String resolveEmail(UserDetails principal) {
        if (principal == null) {
            throw new AuthenticatedException("User is not authenticated");
        }
        return principal.getUsername();
    }
}
