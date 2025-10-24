package vn.cineshow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.cineshow.model.ShowTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    // (Tuỳ chọn) Lọc theo khoảng thời gian
    @EntityGraph(attributePaths = {"room", "movie", "subtitle"})
    List<ShowTime> findByMovie_IdAndStartTimeBetweenOrderByStartTimeAsc(
            Long movieId, LocalDateTime from, LocalDateTime to
    );

    @Query("""
               SELECT st FROM ShowTime st
               JOIN FETCH st.movie m
               LEFT JOIN FETCH st.room r
               LEFT JOIN FETCH st.subtitle s
               WHERE (:movieId IS NULL OR m.id = :movieId)
                 AND (:from IS NULL OR st.startTime >= :from)
                 AND (:to   IS NULL OR st.startTime <= :to)
               ORDER BY st.startTime ASC
            """)
    List<ShowTime> search(@Param("movieId") Long movieId,
                          @Param("from") LocalDateTime from,
                          @Param("to") LocalDateTime to);


    // Tất cả showtime giao cắt [start, end) (dùng cho search theo range)
    @Query("""
            select st from ShowTime st
            join st.room r
            left join r.roomType rt
            where st.startTime < :end
              and st.endTime   > :start
              and (:roomTypeId is null or rt.id = :roomTypeId)
              and (:movieId    is null or st.movie.id = :movieId)
            order by st.startTime asc
            """)
    List<ShowTime> searchOverlap(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("roomTypeId") Long roomTypeId,
            @Param("movieId") Long movieId
    );

    // Chuyên dụng theo NGÀY: [dayStart, dayEnd)
    @Query("""
            select st from ShowTime st
            join st.room r
            left join r.roomType rt
            where st.startTime < :dayEnd
              and st.endTime   > :dayStart
              and (:roomTypeId is null or rt.id = :roomTypeId)
              and (:movieId    is null or st.movie.id = :movieId)
            order by st.startTime asc
            """)
    List<ShowTime> searchByDay(
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd,
            @Param("roomTypeId") Long roomTypeId,
            @Param("movieId") Long movieId
    );

    @Query("""
            SELECT st FROM ShowTime st
             JOIN FETCH st.room r
             JOIN FETCH st.movie m
             JOIN FETCH r.roomType rt
             WHERE
               (:movieId IS NULL OR m.id = :movieId)
               AND (:date IS NULL OR CAST(st.startTime AS date) = :date)
               AND (:roomId IS NULL OR r.id = :roomId)
               AND (:roomTypeId IS NULL OR rt.id = :roomTypeId)
               AND (:startTime IS NULL OR st.startTime >= :startTime)
               AND (:endTime IS NULL OR st.startTime <= :endTime)
             ORDER BY st.startTime ASC
            """)
    List<ShowTime> findShowtimes(
            @Param("movieId") Long movieId,
            @Param("date") LocalDate date,
            @Param("roomId") Long roomId
    );

    @Query("""
            SELECT DISTINCT s.startTime, s.endTime 
            FROM ShowTime s 
            JOIN s.room r
            JOIN r.seats sa
            WHERE FUNCTION('DATE', s.startTime) = :targetDate 
            AND s.movie.id = :movieId 
            AND sa.status = 'AVAILABLE' 
            ORDER BY s.startTime ASC """)
    List<Object[]> findDistinctStartAndEndTimesByDate(@Param("targetDate") LocalDate targetDate, @Param("movieId") Long movieId);

    List<ShowTime> findByMovie_IdAndStartTime(Long movieId, LocalDateTime startTime);
}
