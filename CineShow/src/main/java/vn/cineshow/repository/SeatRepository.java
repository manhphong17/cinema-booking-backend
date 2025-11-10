// File: src/main/java/vn/cineshow/repository/SeatRepository.java

package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
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

    Optional<Seat> findByRoomIdAndRowAndColumn(Long roomId, String row, String column);

    long countByRoomId(Long roomId);

    void deleteByRoomId(Long roomId);

    List<Seat> findAllByRoomId(Long roomId);

    List<Seat> findByRoom(Room room);

}
