package vn.cineshow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.cineshow.dto.response.BDashbroad.MonthlyStatsDTO;
import vn.cineshow.dto.response.BDashbroad.TopProductDTO;
import vn.cineshow.enums.OrderStatus;
import vn.cineshow.model.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // ================== CRUD / BASIC QUERIES ==================

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
    Page<Order> findDistinctByTickets_ShowTime_StartTimeBetween(LocalDateTime start,
                                                                LocalDateTime end,
                                                                Pageable pageable);

    @EntityGraph(attributePaths = {
            "tickets.seat",
            "tickets.showTime.movie",
            "tickets.showTime.room"
    })
    Page<Order> findByCreatedAtBetween(LocalDateTime start,
                                       LocalDateTime end,
                                       Pageable pageable);

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
    Page<Order> findByUser_IdAndCreatedAtBetween(Long userId,
                                                 LocalDateTime start,
                                                 LocalDateTime end,
                                                 Pageable pageable);

    @EntityGraph(attributePaths = {
            "user",
            "tickets.seat",
            "tickets.showTime.movie",
            "tickets.showTime.room"
    })
    @Query("SELECT o FROM Order o " +
            "WHERE o.user.id = :userId " +
            "AND o.createdAt >= :start AND o.createdAt < :end " +
            "AND o.orderStatus IN (vn.cineshow.enums.OrderStatus.COMPLETED, vn.cineshow.enums.OrderStatus.CANCELED) " +
            "ORDER BY o.createdAt DESC")
    Page<Order> findByUser_IdAndCreatedAtBetweenAndStatusIn(@Param("userId") Long userId,
                                                             @Param("start") LocalDateTime start,
                                                             @Param("end") LocalDateTime end,
                                                             Pageable pageable);

    @EntityGraph(attributePaths = {
            "user",
            "tickets.seat",
            "tickets.showTime.movie",
            "tickets.showTime.room"
    })
    @Query("SELECT o FROM Order o " +
            "WHERE o.createdAt >= :start AND o.createdAt < :end " +
            "AND o.orderStatus IN (vn.cineshow.enums.OrderStatus.COMPLETED, vn.cineshow.enums.OrderStatus.CANCELED) " +
            "ORDER BY o.createdAt DESC")
    Page<Order> findByCreatedAtBetweenAndStatusIn(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end,
                                                   Pageable pageable);

    @EntityGraph(attributePaths = {
            "user",
            "user.account"
    })
    @Query("SELECT o FROM Order o " +
            "WHERE (:userId IS NULL OR o.user.id = :userId) " +
            "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
            "ORDER BY o.createdAt DESC")
    Page<Order> findOrdersWithFilters(@Param("userId") Long userId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      Pageable pageable);

    // ================== DASHBOARD / STATS (V1) ==================

    // Đếm số order tạo trong khoảng thời gian
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Tổng doanh thu từ order COMPLETED trong khoảng ngày (JPQL)
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0.0) FROM Order o " +
            "WHERE o.createdAt >= :start AND o.createdAt < :end " +
            "AND o.orderStatus = vn.cineshow.enums.OrderStatus.COMPLETED")
    Double sumRevenueByCreatedAtBetween(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    // Thống kê orders theo ngày (native)
    @Query(value = "SELECT CAST(o.created_at AS DATE) as date, " +
            "COUNT(o.id) as orderCount, " +
            "COALESCE(SUM(CASE WHEN o.order_status = 'COMPLETED' THEN o.total_price ELSE 0.0 END), 0.0) as revenue " +
            "FROM orders o " +
            "WHERE o.created_at >= :start AND o.created_at < :end " +
            "GROUP BY CAST(o.created_at AS DATE) " +
            "ORDER BY date ASC",
            nativeQuery = true)
    List<Object[]> getOrderStatsByDateRange(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    // ================== DASHBOARD / STATS (V2 – BDashbroad) ==================

    @Query("""
        SELECT SUM(o.totalPrice) FROM Order o
        WHERE YEAR(o.updatedAt) = :year AND MONTH(o.updatedAt) = :month
          AND o.orderStatus = 'COMPLETED'
        """)
    Long sumRevenueByMonth(@Param("year") int year, @Param("month") int month);

    @Query("""
        SELECT COUNT(o) FROM Order o
        WHERE YEAR(o.updatedAt) = :year AND MONTH(o.updatedAt) = :month
          AND o.orderStatus = 'COMPLETED'
        """)
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
        GROUP BY c.id, c.name, c.urlImage
        ORDER BY SUM(oc.quantity) DESC
        """)
    List<TopProductDTO> findTopProductsOfCurrentMonth(Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        LEFT JOIN FETCH o.user
        LEFT JOIN FETCH o.payment
        WHERE (:status IS NULL OR o.orderStatus = :status)
          AND o.createdAt BETWEEN :startOfDay AND :endOfDay
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findAllByStatusAndDateRange(@Param("status") OrderStatus status,
                                            @Param("startOfDay") LocalDateTime startOfDay,
                                            @Param("endOfDay") LocalDateTime endOfDay,
                                            Pageable pageable);

    @Query("""
        SELECT SUM(o.totalPrice)
        FROM Order o
        WHERE o.orderStatus = 'COMPLETED'
          AND o.createdAt BETWEEN :start AND :end
        """)
    Double getRevenueByDate(@Param("start") LocalDateTime start,
                            @Param("end") LocalDateTime end);

    @Query("""
        SELECT COUNT(t)
        FROM Ticket t
        WHERE t.order.orderStatus = 'COMPLETED'
          AND t.createdAt BETWEEN :start AND :end
        """)
    Long getTicketsByDate(@Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);

    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.orderStatus = 'COMPLETED'
          AND o.createdAt BETWEEN :start AND :end
        """)
    Long getCompletedOrdersByDate(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    @Query("""
        SELECT COALESCE(SUM(oc.quantity), 0)
        FROM OrderConcession oc
        WHERE oc.order.orderStatus = 'COMPLETED'
          AND oc.order.createdAt BETWEEN :start AND :end
        """)
    Long getConcessionSoldByDate(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);


    @Query("""
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.tickets t
        LEFT JOIN FETCH t.seat s
        LEFT JOIN FETCH s.seatType
        LEFT JOIN FETCH t.showTime st
        LEFT JOIN FETCH st.movie m
        LEFT JOIN FETCH st.room r
        WHERE o.code = :code
        """)
    Optional<Order> findByCodeWithTickets(@Param("code") String code);
}
