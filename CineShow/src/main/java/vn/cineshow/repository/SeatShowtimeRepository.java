package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.SeatShowTime;

@Repository
public interface SeatShowtimeRepository extends JpaRepository<SeatShowTime, Long> {
}
