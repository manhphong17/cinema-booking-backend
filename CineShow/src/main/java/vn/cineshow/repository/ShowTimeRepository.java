package vn.cineshow.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.cineshow.dto.response.booking.ShowTimeResponse;
import vn.cineshow.model.ShowTime;

@Repository
public interface ShowTimeRepository extends JpaRepository<ShowTime, Long> {

    @Override
    @EntityGraph(attributePaths = {"movie", "room", "subtitle"})
    Page<ShowTime> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"movie", "room", "subtitle"})
    List<ShowTime> findAllBy();

    @Query("SELECT st FROM ShowTime st " +
            "JOIN FETCH st.room r " +
            "JOIN FETCH st.movie m " +
            "JOIN FETCH r.roomType rt " +
            "WHERE " +
            "  (:movieId IS NULL OR m.id = :movieId) " +
            "  AND (:date IS NULL OR CAST(st.startTime AS date) = :date) " +
            "  AND (:roomId IS NULL OR r.id = :roomId) " +
            "  AND (:roomTypeId IS NULL OR rt.id = :roomTypeId) " +
            "  AND (:startTime IS NULL OR st.startTime >= :startTime) " +
            "  AND (:endTime IS NULL OR st.startTime <= :endTime) " +
            "ORDER BY st.startTime ASC")
    List<ShowTime> findShowtimes(
            @Param("movieId") Long movieId,
            @Param("date") LocalDate date,
            @Param("roomId") Long roomId,
            @Param("roomTypeId") Long roomTypeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
                SELECT st FROM ShowTime st
                LEFT JOIN FETCH st.room r
                LEFT JOIN FETCH r.roomType rt
                LEFT JOIN FETCH st.movie m
                LEFT JOIN FETCH st.subtitle s
                WHERE st.id = :id
            """)
    Optional<ShowTime> findByIdFetchAll(@Param("id") Long id);


    @Query("""
                SELECT CASE WHEN COUNT(st) > 0 THEN TRUE ELSE FALSE END
                FROM ShowTime st
                WHERE st.room.id = :roomId
                  AND st.startTime < :end
                  AND st.endTime   > :start
            """)
    boolean existsOverlapInRoom(@Param("roomId") Long roomId,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    // (Dành cho update): bỏ qua chính nó
    @Query("""
                SELECT CASE WHEN COUNT(st) > 0 THEN TRUE ELSE FALSE END
                FROM ShowTime st
                WHERE st.room.id = :roomId
                  AND st.id <> :excludeId
                  AND st.startTime < :end
                  AND st.endTime   > :start
            """)
    boolean existsOverlapInRoomExcludingId(@Param("roomId") Long roomId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("excludeId") Long excludeId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ShowTime s
               set s.isDeleted = false
             where s.id = :id and s.isDeleted = true
            """)
    int restore(@Param("id") Long id);

    @Query("""
            select distinct st.movie.id
            from ShowTime st
            where st.isDeleted = false
              and st.startTime >= :from
            """)
    List<Long> findDistinctMovieIdsForUpcoming(@Param("from") LocalDateTime from);

    @Query("""
            SELECT new vn.cineshow.dto.response.booking.ShowTimeResponse(
                s.id,
                s.startTime,
                s.endTime,
                r.id,
                r.name,
                rt.name,
                COUNT(t.id),
                SUM(CASE WHEN t.status = 'AVAILABLE' THEN 1 ELSE 0 END)
            )
            FROM ShowTime s
            JOIN s.room r
            JOIN r.roomType rt
            JOIN s.tickets t
            WHERE s.movie.id = :movieId
              AND r.status = 'ACTIVE'
              AND FUNCTION('DATE', s.startTime) = :targetDate
              AND (:minStartTime IS NULL OR s.startTime > :minStartTime)
            GROUP BY s.id, s.startTime, s.endTime, r.id, r.name, rt.name
            ORDER BY s.startTime ASC
            """)
    List<ShowTimeResponse> findUpcomingShowTimes(
            @Param("targetDate") LocalDate targetDate,
            @Param("minStartTime") LocalDateTime minStartTime,
            @Param("movieId") Long movieId);


    List<ShowTime> findByMovie_IdAndStartTime(Long movieId, LocalDateTime startTime);

}
