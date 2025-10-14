package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.Movie;

import java.time.LocalDate;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByNameAndReleaseDate(String name, LocalDate releaseDate);
}
