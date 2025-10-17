package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.SeatType;

import java.util.Optional;

@Repository
public interface SeatTypeRepository extends JpaRepository<SeatType, Long> {

    Optional<SeatType> findByCode(String code);

    Optional<SeatType> findByNameIgnoreCase(String name);

    boolean existsByCode(String code);
}
