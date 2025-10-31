package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.Account;
import vn.cineshow.model.RefreshToken;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByAccount(Account account);

    Optional<RefreshToken> findByAccount(Account account);

    Optional<RefreshToken> findByAccountId(Long accountId);
}
