package vn.cineshow.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cineshow.dto.response.IdNameDTO;
import vn.cineshow.enums.MovieStatus;
import vn.cineshow.model.Movie;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByNameAndReleaseDate(String name, LocalDate releaseDate);

    List<Movie> findByStatusIn(Collection<MovieStatus> statuses, Sort sort);


    @Query("SELECT m FROM Movie m  WHERE m.isFeatured = true")
    List<Movie> findAllFeaturedMovies();

    @Query("select m from Movie m where m.status =:status ORDER BY m.releaseDate desc")
    List<Movie> findTopNMovieByStatus(@Param("status") MovieStatus status, Pageable pageable);

    @Query("select new vn.cineshow.dto.response.IdNameDTO(m.id, m.name) " +
            "from Movie m " +
            "where m.status in :statuses and m.isDeleted = false " +
            "order by m.name asc")
    List<IdNameDTO> findAllIdNameByStatuses(@Param("statuses") List<MovieStatus> statuses);

    boolean existsByNameAndReleaseDateAndIdNot(String name, LocalDate releaseDate, Long id);
}
