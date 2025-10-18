package vn.cineshow.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.Movie;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    @Override
    @EntityGraph()
    List<Movie> findAll();

    boolean existsByNameAndReleaseDate(String name, LocalDate releaseDate);
}
