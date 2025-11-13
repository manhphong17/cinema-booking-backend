package vn.cineshow.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.cineshow.enums.TicketStatus;
import vn.cineshow.model.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t FROM Ticket t " +
           "LEFT JOIN FETCH t.seat s " +
           "LEFT JOIN FETCH s.seatType " +
           "WHERE t.id = :id")
    Optional<Ticket> findByIdWithSeat(@Param("id") Long id);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.showTime.id = :showTimeId AND t.status = :status")
    Long countByShowTime_IdAndStatus(@Param("showTimeId") Long showTimeId, @Param("status") TicketStatus status);

    @Query("""
    SELECT t FROM Ticket t
    JOIN FETCH t.seat s
    JOIN FETCH s.seatType st
    JOIN FETCH s.room r
    JOIN FETCH r.roomType rt
    JOIN FETCH t.ticketPrice tp
    JOIN FETCH t.showTime sh
    JOIN FETCH sh.movie m
    WHERE t.id IN :ids
""")
    List<Ticket> findTicketsWithRelations(@Param("ids") List<Long> ids);

    /**
     * Tìm tất cả ticket có status AVAILABLE hoặc BLOCKED
     * của các showtime đã qua ngày suất chiếu (endTime < threshold date)
     */
    @Query("""
        SELECT t FROM Ticket t
        JOIN FETCH t.showTime st
        WHERE t.status IN :statuses
        AND CAST(st.endTime AS date) < :thresholdDate
    """)
    List<Ticket> findAvailableOrBlockedTicketsAfterShowtimeDate(
            @Param("statuses") List<TicketStatus> statuses,
            @Param("thresholdDate") LocalDate thresholdDate
    );

}
