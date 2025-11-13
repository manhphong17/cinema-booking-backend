package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.request.room.RoomRequest;
import vn.cineshow.dto.response.PageResponse;
import vn.cineshow.dto.response.room.RoomDTO;
import vn.cineshow.dto.response.room.room_type.RoomTypeDTO;
import vn.cineshow.enums.RoomStatus;
import vn.cineshow.enums.SeatStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.Room;
import vn.cineshow.model.RoomType;
import vn.cineshow.model.Seat;
import vn.cineshow.model.SeatType;
import vn.cineshow.repository.*;
import vn.cineshow.service.RoomService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final SeatRepository seatRepository;
    private final ShowTimeRepository showTimeRepository;
    private final SeatTypeRepository seatTypeRepository;

    private static final int DEFAULT_PAGE_NO = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_ROOM_DIMENSION = 12;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name", "createdAt", "updatedAt", "rows", "columns", "capacity", "status"
    );

    @Override
    @Transactional(readOnly = true)
    public PageResponse<List<RoomDTO>> searchRooms(Integer pageNo,
                                                   Integer pageSize,
                                                   String keyword,
                                                   Long roomTypeId,
                                                   String status,
                                                   String sortBy) {
        int p = (pageNo == null || pageNo < 0) ? DEFAULT_PAGE_NO : pageNo;
        int s = (pageSize == null || pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;
        Pageable pageable = PageRequest.of(p, s, parseSort(sortBy));

        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Long typeId = (roomTypeId != null && roomTypeId > 0) ? roomTypeId : null;

        RoomStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = RoomStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // nếu FE gửi sai status -> bỏ filter theo status
            }
        }

        var page = roomRepository.findFilteredRooms(kw, typeId, statusEnum, pageable);

        var items = page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResponse.<List<RoomDTO>>builder()
                .pageNo(p)
                .pageSize(s)
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .items(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDTO getRoomDetail(Long roomId) {
        return roomRepository.findById(roomId)
                .map(this::toDTO)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
    }

    @Override
    @Transactional
    public RoomDTO createRoom(RoomRequest request) {
        if (request.getRows() > MAX_ROOM_DIMENSION || request.getColumns() > MAX_ROOM_DIMENSION) {
            throw new AppException(ErrorCode.ROOM_SIZE_EXCEEDED);
        }
        if (roomRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.ROOM_ALREADY_EXISTED);
        }
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        Room entity = new Room();
        entity.setName(request.getName());
        entity.setRoomType(roomType);
        entity.setRows(request.getRows());
        entity.setColumns(request.getColumns());
        
        // String -> Enum
        entity.setStatus(RoomStatus.valueOf(request.getStatus().trim().toUpperCase()));

        entity.setDescription(request.getDescription());

        Room saved = roomRepository.save(entity);

        SeatType defaultSeatType = seatTypeRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND));

        int rows = request.getRows();
        int cols = request.getColumns();

        List<Seat> batch = new ArrayList<>(rows * cols);
        for (int r = 1; r <= rows; r++) {
            for (int c = 1; c <= cols; c++) {
                Seat s = new Seat();
                s.setRoom(saved);
                s.setRow(String.valueOf(r));
                s.setColumn(String.valueOf(c));
                s.setSeatType(defaultSeatType);
                s.setStatus(SeatStatus.AVAILABLE);
                batch.add(s);
            }
        }
        seatRepository.saveAll(batch);

        saved.setCapacity(batch.size());
        roomRepository.save(saved);
        
        return toDTO(saved);
    }

    @Override
    @Transactional
    public RoomDTO updateRoom(Long roomId, RoomRequest request) {
        if (request.getRows() > MAX_ROOM_DIMENSION || request.getColumns() > MAX_ROOM_DIMENSION) {
            throw new AppException(ErrorCode.ROOM_SIZE_EXCEEDED);
        }
        Room entity = roomRepository.findById(roomId).orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        if (showTimeRepository.existsByRoom_Id(roomId)) {
            throw new AppException(ErrorCode.ROOM_IN_USE);
        }

        if (request.getRoomTypeId() != null) {
            RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));
            entity.setRoomType(roomType);
        }

        entity.setName(request.getName());
        entity.setRows(request.getRows());
        entity.setColumns(request.getColumns());

        // String -> Enum
        entity.setStatus(RoomStatus.valueOf(request.getStatus().trim().toUpperCase()));

        entity.setDescription(request.getDescription());

        Room saved = roomRepository.save(entity);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public boolean deleteRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new AppException(ErrorCode.ROOM_NOT_FOUND);
        }
        if (showTimeRepository.existsByRoom_Id(roomId)) {
            throw new AppException(ErrorCode.ROOM_IN_USE);
        }
        roomRepository.deleteById(roomId);
        return true;
    }

    /* ===== Helpers ===== */

    private Sort parseSort(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) return Sort.by(Sort.Order.desc("createdAt"));
        List<Sort.Order> orders = new ArrayList<>();
        for (String token : sortBy.split(",")) {
            String[] parts = token.trim().split(":");
            if (parts.length == 0) continue;
            String field = parts[0].trim();
            String dir = (parts.length > 1) ? parts[1].trim() : "asc";
            if (!ALLOWED_SORT_FIELDS.contains(field)) continue;
            orders.add("desc".equalsIgnoreCase(dir) ? Sort.Order.desc(field) : Sort.Order.asc(field));
        }
        return orders.isEmpty() ? Sort.by(Sort.Order.desc("createdAt")) : Sort.by(orders);
    }

    private RoomDTO toDTO(Room room) {
        RoomTypeDTO rtDTO = null;
        RoomType rt = room.getRoomType();
        if (rt != null) {
            rtDTO = RoomTypeDTO.builder()
                    .id(rt.getId())
                    .name(rt.getName())
                    .description(rt.getDescription())
                    .build();
        }

        return RoomDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .roomType(rtDTO)
                .rows(room.getRows())
                .columns(room.getColumns())
                .capacity(room.getCapacity())
                // Enum -> String cho DTO
                .status(room.getStatus() == null ? null : room.getStatus().name())
                .description(room.getDescription())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
