package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.cineshow.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;


public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    boolean existsByIdAndUser_Id(Long orderId, Long userId);

    @EntityGraph(attributePaths = {
            "tickets.seat",
            "tickets.showTime.movie",
            "tickets.showTime.room"
    })
    Page<Order> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = {
            "user",
            "tickets.seat",
            "tickets.showTime.movie",
            "tickets.showTime.room"
    })
    Optional<Order> findOneById(Long id);

    @EntityGraph(attributePaths = {
            "tickets.seat",
            "tickets.showTime.movie",
            "tickets.showTime.room"
    })
    Page<Order> findDistinctByTickets_ShowTime_StartTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @EntityGraph(attributePaths = {
            "tickets.seat",
            "tickets.showTime.movie",
            "tickets.showTime.room"
    })
    Page<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT o FROM Order o " +
            "WHERE o.orderStatus = vn.cineshow.enums.OrderStatus.PENDING " +
            "AND o.updatedAt < :threshold")
    List<Order> findPendingBefore(@Param("threshold") LocalDateTime threshold);

    @EntityGraph(attributePaths = {
            "user",
            "tickets.seat",
            "tickets.showTime.movie",
            "tickets.showTime.room"
    })
    Page<Order> findByUser_IdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
