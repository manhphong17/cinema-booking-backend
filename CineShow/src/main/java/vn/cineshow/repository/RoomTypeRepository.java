package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.RoomType;

import java.util.Optional;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    Optional<RoomType> findByCode(String code);

    boolean existsByCode(String code);
}
