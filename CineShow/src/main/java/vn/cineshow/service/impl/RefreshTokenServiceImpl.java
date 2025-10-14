package vn.cineshow.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.cineshow.model.Account;
import vn.cineshow.model.RefreshToken;
import vn.cineshow.repository.RefreshTokenRepository;
import vn.cineshow.service.RefreshTokenService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void replaceRefreshToken(Account account, String token, long expirySeconds) {

        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByAccount(account);

        if (optionalRefreshToken.isPresent()) {
            RefreshToken refreshToken = optionalRefreshToken.get();
            refreshToken.setToken(token);
            refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(expirySeconds));
            refreshTokenRepository.save(refreshToken);
        } else {
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setAccount(account);
            refreshToken.setToken(token);
            refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(expirySeconds));
            refreshTokenRepository.save(refreshToken);
        }

    }

}
