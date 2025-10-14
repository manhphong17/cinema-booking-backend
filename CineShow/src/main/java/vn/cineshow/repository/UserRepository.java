package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.cineshow.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByAccount_Email(String email);
}

