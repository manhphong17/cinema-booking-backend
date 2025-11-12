package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.cineshow.model.Theater;

public interface TheaterRepository extends JpaRepository<Theater, Long> {

}
