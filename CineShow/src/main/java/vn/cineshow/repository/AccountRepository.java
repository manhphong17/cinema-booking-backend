package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.Account;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<UserDetails> findByEmail(String email);

    Optional<Account> findAccountByEmail(String email);

    // Count all active, non-deleted accounts
    long countByIsDeletedFalse();

    // Count accounts created after a certain time
    long countByCreatedAtAfterAndIsDeletedFalse(LocalDateTime from);

    // Count accounts updated today (proxy for login activity)
    @Query("SELECT COUNT(DISTINCT a.id) FROM Account a " +
            "WHERE a.updatedAt >= :from AND a.updatedAt < :to AND a.isDeleted = false")
    long countActiveAccountsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Count accounts created per day in a date range
    @Query(value = "SELECT CAST(a.created_at AS DATE) as date, COUNT(a.id) as count " +
            "FROM accounts a " +
            "WHERE a.created_at >= :from AND a.created_at < :to AND a.is_deleted = false " +
            "GROUP BY CAST(a.created_at AS DATE) " +
            "ORDER BY date ASC", nativeQuery = true)
    java.util.List<Object[]> countAccountsByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Count accounts updated per day (proxy for login activity)
    @Query(value = "SELECT CAST(a.updated_at AS DATE) as date, COUNT(DISTINCT a.id) as count " +
            "FROM accounts a " +
            "WHERE a.updated_at >= :from AND a.updated_at < :to AND a.is_deleted = false " +
            "GROUP BY CAST(a.updated_at AS DATE) " +
            "ORDER BY date ASC", nativeQuery = true)
    java.util.List<Object[]> countActiveAccountsByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Find accounts with filter by created date and pagination
    @Query("SELECT a FROM Account a WHERE " +
            "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR a.createdAt <= :endDate) " +
            "ORDER BY a.createdAt DESC")
    org.springframework.data.domain.Page<Account> findAccountsWithFilter(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            org.springframework.data.domain.Pageable pageable);
}