package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cineshow.enums.SeatStatus;
import vn.cineshow.model.Room;
import vn.cineshow.model.Seat;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByRoom_Id(Long roomId);

    void deleteByRoom_Id(Long roomId);

    List<Seat> findByRoom_IdOrderByRowAscColumnAsc(Long roomId);

    Optional<Seat> findByRoom_IdAndRowAndColumn(Long roomId, String row, String column);


    List<Seat> findByRoom(Room room);

    List<Seat> findBySeatType_Id(Long seatTypeId);

    long countByRoomId(Long roomId);

    long countByRoomIdAndStatus(Long roomId, SeatStatus status);

}
