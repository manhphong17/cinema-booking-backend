package vn.cineshow.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cineshow.enums.MovieStatus;
import vn.cineshow.model.Movie;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByNameAndReleaseDate(String name, LocalDate releaseDate);

    List<Movie> findAllByIsFeatured(Boolean isFeatured);

    @Query("SELECT m FROM Movie m  WHERE m.isFeatured = true")
    List<Movie> findAllFeaturedMovies();

    @Query("select m from Movie m where m.status =:status ORDER BY m.releaseDate desc")
    List<Movie> findTopNMovieByStatus(@Param("status") MovieStatus status, Pageable pageable);


}
