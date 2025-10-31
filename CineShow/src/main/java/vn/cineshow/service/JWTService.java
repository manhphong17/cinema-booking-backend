package vn.cineshow.service;

import vn.cineshow.enums.TokenType;

import java.util.List;

public interface JWTService {
    String generateAccessToken(String email, List<String> authorities, Long userId);

    String generateRefreshToken(String email, List<String> authorities);

    String extractUsername(String token, TokenType tokenType);

    long getAccessTokenExpiryInSecond();

    long getRefreshTokenExpiryInSecond();
}
