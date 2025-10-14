package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.cineshow.model.MovieGenre;

public interface MovieGenresRepository extends JpaRepository<MovieGenre, Long> {
}
