package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.MovieGenre;

@Repository
public interface MovieGenresRepository extends JpaRepository<MovieGenre, Long> {
}
