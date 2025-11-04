package vn.cineshow.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.cineshow.dto.request.UpdateUserRequest;
import vn.cineshow.dto.response.UserResponse;
import vn.cineshow.exception.ResourceNotFoundException;
import vn.cineshow.model.User;
import vn.cineshow.repository.UserRepository;
import vn.cineshow.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public String getNameByAccountEmail(String email) {
        return userRepository.findByAccount_Email(email)
                .map(User::getName)
                .orElse("Friend");
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        User user = getUserByEmail(email);
        return mapToResponse(user);
    }

    @Transactional
    @Override
    public UserResponse updateProfile(String email, UpdateUserRequest request) {
        User user = getUserByEmail(email);
        user.setName(request.getName());
        user.setAddress(request.getAddress());
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }

        userRepository.save(user);
        return mapToResponse(user);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByAccount_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getAccount() != null ? user.getAccount().getEmail() : null)
                .address(user.getAddress())
                .loyalPoint(user.getLoyalPoint())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .build();
    }
}

