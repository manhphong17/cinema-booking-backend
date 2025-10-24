package vn.cineshow.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.cineshow.enums.MovieStatus;
import vn.cineshow.model.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByNameAndReleaseDate(String name, LocalDate releaseDate);
    
    boolean existsByNameAndReleaseDateAndIdNot(String name, LocalDate releaseDate, Long id);

    List<Movie> findAllByIsFeatured(Boolean isFeatured);

    @Query("SELECT m FROM Movie m  WHERE m.isFeatured = true")
    List<Movie> findAllFeaturedMovies();

    @Query("select m from Movie m where m.status =:status ORDER BY m.releaseDate desc")
    List<Movie> findTopNMovieByStatus(@Param("status") MovieStatus status, Pageable pageable);


}
