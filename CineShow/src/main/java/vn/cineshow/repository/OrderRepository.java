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

    // Count orders created in a date range
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Sum revenue from completed orders in a date range
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0.0) FROM Order o " +
           "WHERE o.createdAt >= :start AND o.createdAt < :end " +
           "AND o.orderStatus = vn.cineshow.enums.OrderStatus.COMPLETED")
    Double sumRevenueByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Count orders and revenue per day
    @Query(value = "SELECT CAST(o.created_at AS DATE) as date, " +
           "COUNT(o.id) as orderCount, " +
           "COALESCE(SUM(CASE WHEN o.order_status = 'COMPLETED' THEN o.total_price ELSE 0.0 END), 0.0) as revenue " +
           "FROM orders o " +
           "WHERE o.created_at >= :start AND o.created_at < :end " +
           "GROUP BY CAST(o.created_at AS DATE) " +
           "ORDER BY date ASC", nativeQuery = true)
    java.util.List<Object[]> getOrderStatsByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
