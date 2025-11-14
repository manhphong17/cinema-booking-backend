package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.request.room.RoomTypeCreateRequest;
import vn.cineshow.dto.request.room.RoomTypeUpdateRequest;
import vn.cineshow.dto.response.room.room_type.RoomTypeDTO;
import vn.cineshow.dto.response.room.room_type.RoomTypeResponse;
import vn.cineshow.enums.RoomStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.Room;
import vn.cineshow.model.RoomType;
import vn.cineshow.repository.RoomRepository;
import vn.cineshow.repository.RoomTypeRepository;
import vn.cineshow.repository.ShowTimeRepository;
import vn.cineshow.service.RoomTypeService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final ShowTimeRepository showTimeRepository;


    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeDTO> getAllRoomTypesDTO() {
        // Sắp xếp theo tên cho ổn định UI; nếu không cần có thể dùng findAll() đơn thuần
        List<RoomType> entities = roomTypeRepository.findAll(Sort.by(Sort.Order.asc("name")));
        return entities.stream()
                .map(rt -> RoomTypeDTO.builder()
                        .id(rt.getId())
                        .name(rt.getName())
                        .description(rt.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    private RoomTypeResponse map(RoomType e) {
        return RoomTypeResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .active(Boolean.TRUE.equals(e.getActive()))
                .build();
    }

    @Override @Transactional(readOnly = true)
    public List<RoomTypeResponse> findAll(Boolean onlyActive) {
        var list = roomTypeRepository.findAll();
        if (Boolean.TRUE.equals(onlyActive)) {
            list = list.stream().filter(rt -> Boolean.TRUE.equals(rt.getActive())).toList();
        }
        return list.stream().map(this::map).toList();
    }

    @Override @Transactional(readOnly = true)
    public RoomTypeResponse findById(Long id) {
        var e = roomTypeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));
        return map(e);
    }

    @Override
    @Transactional
    public RoomTypeResponse create(RoomTypeCreateRequest req) {
        if (req.getName() == null || req.getName().isBlank()) throw new AppException(ErrorCode.INVALID_PARAMETER);
        var name = req.getName().trim();
        if (roomTypeRepository.existsByNameIgnoreCase(name)) throw new AppException(ErrorCode.ROOM_TYPE_ALREADY_EXISTED);

        var e = RoomType.builder()
                .name(name)
                .description(req.getDescription())
                .active(req.getActive() == null ? true : req.getActive())
                .build();
        roomTypeRepository.save(e);
        return map(e);
    }

    @Override
    @Transactional
    public RoomTypeResponse update(Long id, RoomTypeUpdateRequest req) {
        var e = roomTypeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));
        if (showTimeRepository.existsByRoom_RoomType_Id(id)) {
            throw new AppException(ErrorCode.ROOM_TYPE_HAS_SHOWTIMES);
        }
        if (req.getName() == null || req.getName().isBlank()) throw new AppException(ErrorCode.INVALID_PARAMETER);
        var newName = req.getName().trim();
        if (roomTypeRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) throw new AppException(ErrorCode.ROOM_TYPE_ALREADY_EXISTED);

        e.setName(newName);
        e.setDescription(req.getDescription());
        if (req.getActive() != null) e.setActive(req.getActive());
        return map(e);
    }

    @Override
    @Transactional
    public RoomTypeResponse patch(Long id, RoomTypeUpdateRequest req) {
        var e = roomTypeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));
        if (showTimeRepository.existsByRoom_RoomType_Id(id)) {
            throw new AppException(ErrorCode.ROOM_TYPE_HAS_SHOWTIMES);
        }
        if (req.getName() != null) {
            var newName = req.getName().trim();
            if (newName.isBlank()) throw new AppException(ErrorCode.INVALID_PARAMETER);
            if (roomTypeRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) throw new AppException(ErrorCode.ROOM_TYPE_ALREADY_EXISTED);
            e.setName(newName);
        }
        if (req.getDescription() != null) e.setDescription(req.getDescription());
        if (req.getActive() != null) e.setActive(req.getActive());
        return map(e);
    }

    @Override @Transactional public void delete(Long id) {
        if (!roomTypeRepository.existsById(id)) {
            throw new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND);
        }
        if (showTimeRepository.existsByRoom_RoomType_Id(id)) {
            throw new AppException(ErrorCode.ROOM_TYPE_HAS_SHOWTIMES);
        }
        if (roomRepository.findByRoomType_Id(id, Sort.unsorted()) != null) {
            throw new AppException(ErrorCode.ROOM_TYPE_IN_USE);
        }
        roomTypeRepository.deleteById(id);
    }

    @Override @Transactional public RoomTypeResponse activate(Long id)   { var e = roomTypeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND)); e.setActive(true);  return map(e); }
    @Override @Transactional public RoomTypeResponse deactivate(Long id) {
        if (showTimeRepository.existsByRoom_RoomType_Id(id)) {
            throw new AppException(ErrorCode.ROOM_TYPE_HAS_SHOWTIMES);
        }
        var e = roomTypeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));
        e.setActive(false);

        List<Room> roomsToDeactivate = roomRepository.findAllByRoomType_Id(id);
        for (Room room : roomsToDeactivate) {
            room.setStatus(RoomStatus.INACTIVE);
        }
        roomRepository.saveAll(roomsToDeactivate);

        return map(e);
    }
}
