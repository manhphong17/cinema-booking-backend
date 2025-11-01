package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cineshow.enums.SeatShowTimeStatus;
import vn.cineshow.model.Ticket;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t FROM Ticket t " +
           "LEFT JOIN FETCH t.seat s " +
           "LEFT JOIN FETCH s.seatType " +
           "WHERE t.id = :id")
    Optional<Ticket> findByIdWithSeat(@Param("id") Long id);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.showTime.id = :showTimeId AND t.status = :status")
    Long countByShowTime_IdAndStatus(@Param("showTimeId") Long showTimeId, @Param("status") SeatShowTimeStatus status);

}
