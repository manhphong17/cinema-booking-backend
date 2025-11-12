package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.cineshow.dto.response.BDashbroad.MonthlyStatsDTO;
import vn.cineshow.dto.response.BDashbroad.TopProductDTO;
import vn.cineshow.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.YearMonth;
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


    @Query("SELECT SUM(o.totalPrice) FROM Order o " +
            "WHERE YEAR(o.updatedAt) = :year AND MONTH(o.updatedAt) = :month " +
            "AND o.orderStatus = 'COMPLETED'")
    Long sumRevenueByMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE YEAR(o.updatedAt) = :year AND MONTH(o.updatedAt) = :month " +
            "AND o.orderStatus = 'COMPLETED'")
    Long countByMonth(@Param("year") int year, @Param("month") int month);


    @Query("""
    SELECT new vn.cineshow.dto.response.BDashbroad.MonthlyStatsDTO(
        MONTH(o.updatedAt),
        COALESCE(SUM(o.totalPrice), 0)
    )
    FROM Order o
    WHERE YEAR(o.updatedAt) = YEAR(CURRENT_DATE)
      AND o.orderStatus = 'COMPLETED'
    GROUP BY MONTH(o.updatedAt)
    ORDER BY MONTH(o.updatedAt)
""")
    List<MonthlyStatsDTO> getRevenueOfCurrentYear();


    @Query("""
    SELECT new vn.cineshow.dto.response.BDashbroad.MonthlyStatsDTO(
        MONTH(o.updatedAt),
        COUNT(o)
    )
    FROM Order o
    WHERE YEAR(o.updatedAt) = YEAR(CURRENT_DATE)
      AND o.orderStatus = 'COMPLETED'
    GROUP BY MONTH(o.updatedAt)
    ORDER BY MONTH(o.updatedAt)
""")
    List<MonthlyStatsDTO> getOrdersOfCurrentYear();

    @Query("""
    SELECT new vn.cineshow.dto.response.BDashbroad.TopProductDTO(
        c.name,
        SUM(oc.unitPrice * oc.quantity),
        SUM(oc.quantity),
                c.urlImage
    )
    FROM Order o
    JOIN OrderConcession oc ON o.id = oc.order.id
    JOIN Concession c ON oc.concession.id = c.id
    WHERE o.orderStatus = vn.cineshow.enums.OrderStatus.COMPLETED
      AND YEAR(o.updatedAt) = YEAR(CURRENT_DATE)
      AND MONTH(o.updatedAt) = MONTH(CURRENT_DATE)
    GROUP BY c.id, c.name
    ORDER BY SUM(oc.quantity) DESC
""")
    List<TopProductDTO> findTopProductsOfCurrentMonth(Pageable pageable);

}
