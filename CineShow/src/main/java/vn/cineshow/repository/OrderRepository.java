package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.cineshow.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
}
