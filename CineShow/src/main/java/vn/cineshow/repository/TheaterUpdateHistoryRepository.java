package vn.cineshow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cineshow.model.TheaterUpdateHistory;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TheaterUpdateHistoryRepository extends JpaRepository<TheaterUpdateHistory, Long> {
    
    // Lấy lịch sử theo theater ID (luôn = 1)
    Page<TheaterUpdateHistory> findByTheaterIdOrderByUpdatedAtDesc(Long theaterId, Pageable pageable);
    
    // Lấy lịch sử theo field cụ thể
    List<TheaterUpdateHistory> findByTheaterIdAndUpdatedFieldOrderByUpdatedAtDesc(Long theaterId, String updatedField);
    
    // Lấy lịch sử theo người update
    Page<TheaterUpdateHistory> findByTheaterIdAndUpdatedByOrderByUpdatedAtDesc(Long theaterId, String updatedBy, Pageable pageable);
    
    // Lấy lịch sử trong khoảng thời gian
    Page<TheaterUpdateHistory> findByTheaterIdAndUpdatedAtBetweenOrderByUpdatedAtDesc(
            Long theaterId, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable
    );

    // Lấy lịch sử gần đây nhất (cho dashboard)
    Page<TheaterUpdateHistory> findByOrderByUpdatedAtDesc(Pageable pageable);
}
