package vn.cineshow.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.cineshow.enums.DayType;
import vn.cineshow.model.TicketPrice;
import java.util.List;
import java.util.Optional;

public interface TicketPriceRepository extends JpaRepository<TicketPrice, Long> {
    Optional<TicketPrice> findBySeatTypeIdAndRoomTypeIdAndDayType(Long seatTypeId, Long roomTypeId, DayType dayType);

    @Query("SELECT tp FROM TicketPrice tp JOIN FETCH tp.roomType JOIN FETCH tp.seatType")
    List<TicketPrice> findAllWithRelations();

    @Query("""
        SELECT tp.price 
        FROM TicketPrice tp
        WHERE tp.roomType.id = :roomTypeId
          AND tp.seatType.id = :seatTypeId
          AND tp.dayType = :dayType
    """)
    Optional<Double> findPrice(Long roomTypeId, Long seatTypeId, DayType dayType);
}
