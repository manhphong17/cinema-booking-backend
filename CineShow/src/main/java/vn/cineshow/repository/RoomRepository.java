package vn.cineshow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.cineshow.enums.RoomStatus;
import vn.cineshow.model.Room;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    /**
     * Tìm kiếm phòng có filter tùy chọn:
     * - keyword theo tên phòng
     * - roomTypeId theo r.roomType.id
     * - status (String) theo r.status
     *
     * Nếu project dùng Enum cho status, bạn có thể thay kiểu tham số sang Enum và điều chỉnh so sánh cho phù hợp.
     */
    // RoomRepository.java
    @Query("""
        SELECT r FROM Room r
        WHERE (:keyword IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:roomTypeId IS NULL OR r.roomType.id = :roomTypeId)
          AND (:status IS NULL OR r.status = :status)
    """)
    Page<Room> findFilteredRooms(@Param("keyword") String keyword,
                                 @Param("roomTypeId") Long roomTypeId,
                                 @Param("status") RoomStatus status,
                                 Pageable pageable);


    boolean existsByNameIgnoreCase(String name);

    Object findByRoomType_Id(Long roomTypeId, Sort sort);

    List<Room> findAllByRoomType_Id(Long roomTypeId);
}
