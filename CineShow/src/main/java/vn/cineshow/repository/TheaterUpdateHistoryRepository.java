package vn.cineshow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.TheaterUpdateHistory;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TheaterUpdateHistoryRepository extends JpaRepository<TheaterUpdateHistory, Long> {
    
    // Lấy lịch sử theo theater ID (luôn = 1) - query bằng theater.id
    Page<TheaterUpdateHistory> findByTheater_IdOrderByUpdatedAtDesc(Long theaterId, Pageable pageable);
    
    // Lấy lịch sử theo field cụ thể
    List<TheaterUpdateHistory> findByTheater_IdAndUpdatedFieldOrderByUpdatedAtDesc(Long theaterId, String updatedField);
    
    // Lấy lịch sử theo người update
    Page<TheaterUpdateHistory> findByTheater_IdAndUpdatedByOrderByUpdatedAtDesc(Long theaterId, String updatedBy, Pageable pageable);
    
    // Lấy lịch sử trong khoảng thời gian
    Page<TheaterUpdateHistory> findByTheater_IdAndUpdatedAtBetweenOrderByUpdatedAtDesc(
            Long theaterId, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable
    );

    // Lấy lịch sử gần đây nhất (cho dashboard)
    Page<TheaterUpdateHistory> findByOrderByUpdatedAtDesc(Pageable pageable);
    
    // Backward compatible: giữ lại methods cũ với @Query
    @Query("SELECT h FROM TheaterUpdateHistory h WHERE h.theater.id = :theaterId ORDER BY h.updatedAt DESC")
    Page<TheaterUpdateHistory> findByTheaterIdOrderByUpdatedAtDesc(@Param("theaterId") Long theaterId, Pageable pageable);
    
    @Query("SELECT h FROM TheaterUpdateHistory h WHERE h.theater.id = :theaterId AND h.updatedField = :updatedField ORDER BY h.updatedAt DESC")
    List<TheaterUpdateHistory> findByTheaterIdAndUpdatedFieldOrderByUpdatedAtDesc(@Param("theaterId") Long theaterId, @Param("updatedField") String updatedField);
    
    @Query("SELECT h FROM TheaterUpdateHistory h WHERE h.theater.id = :theaterId AND h.updatedBy = :updatedBy ORDER BY h.updatedAt DESC")
    Page<TheaterUpdateHistory> findByTheaterIdAndUpdatedByOrderByUpdatedAtDesc(@Param("theaterId") Long theaterId, @Param("updatedBy") String updatedBy, Pageable pageable);
    
    @Query("SELECT h FROM TheaterUpdateHistory h WHERE h.theater.id = :theaterId AND h.updatedAt BETWEEN :startDate AND :endDate ORDER BY h.updatedAt DESC")
    Page<TheaterUpdateHistory> findByTheaterIdAndUpdatedAtBetweenOrderByUpdatedAtDesc(
            @Param("theaterId") Long theaterId, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate, 
            Pageable pageable
    );
}
