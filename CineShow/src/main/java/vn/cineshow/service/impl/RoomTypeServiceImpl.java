package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.request.room.RoomTypeCreateRequest;
import vn.cineshow.dto.request.room.RoomTypeUpdateRequest;
import vn.cineshow.dto.response.room.room_type.RoomTypeDTO;
import vn.cineshow.dto.response.room.room_type.RoomTypeResponse;
import vn.cineshow.model.RoomType;
import vn.cineshow.repository.RoomTypeRepository;
import vn.cineshow.service.RoomTypeService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeDTO> getAllRoomTypesDTO() {
        // Sắp xếp theo tên cho ổn định UI; nếu không cần có thể dùng findAll() đơn thuần
        List<RoomType> entities = roomTypeRepository.findAll(Sort.by(Sort.Order.asc("name")));
        return entities.stream()
                .map(rt -> RoomTypeDTO.builder()
                        .id(rt.getId())
//                        .code(rt.getCode())
                        .name(rt.getName())
                        .description(rt.getDescription())
                        .build())
                .collect(Collectors.toList());
    }



    private final RoomTypeRepository repo;

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
        var list = repo.findAll();
        if (Boolean.TRUE.equals(onlyActive)) {
            list = list.stream().filter(rt -> Boolean.TRUE.equals(rt.getActive())).toList();
        }
        return list.stream().map(this::map).toList();
    }

    @Override @Transactional(readOnly = true)
    public RoomTypeResponse findById(Long id) {
        var e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("RoomType not found"));
        return map(e);
    }

    @Override
    @Transactional
    public RoomTypeResponse create(RoomTypeCreateRequest req) {
        if (req.getName() == null || req.getName().isBlank()) throw new IllegalArgumentException("Name is required");
        var name = req.getName().trim();
        if (repo.existsByNameIgnoreCase(name)) throw new IllegalArgumentException("Name already exists");

        var e = RoomType.builder()
                .name(name)
                .description(req.getDescription())
                .active(req.getActive() == null ? true : req.getActive())
                .build();
        repo.save(e);
        return map(e);
    }

    @Override
    @Transactional
    public RoomTypeResponse update(Long id, RoomTypeUpdateRequest req) {
        var e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("RoomType not found"));
        if (req.getName() == null || req.getName().isBlank()) throw new IllegalArgumentException("Name is required");
        var newName = req.getName().trim();
        if (repo.existsByNameIgnoreCaseAndIdNot(newName, id)) throw new IllegalArgumentException("Name already exists");

        e.setName(newName);
        e.setDescription(req.getDescription());
        if (req.getActive() != null) e.setActive(req.getActive());
        return map(e);
    }

    @Override
    @Transactional
    public RoomTypeResponse patch(Long id, RoomTypeUpdateRequest req) {
        var e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("RoomType not found"));
        if (req.getName() != null) {
            var newName = req.getName().trim();
            if (newName.isBlank()) throw new IllegalArgumentException("Name cannot be blank");
            if (repo.existsByNameIgnoreCaseAndIdNot(newName, id)) throw new IllegalArgumentException("Name already exists");
            e.setName(newName);
        }
        if (req.getDescription() != null) e.setDescription(req.getDescription());
        if (req.getActive() != null) e.setActive(req.getActive());
        return map(e);
    }

    @Override @Transactional public void delete(Long id) { repo.deleteById(id); }

    @Override @Transactional public RoomTypeResponse activate(Long id)   { var e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("RoomType not found")); e.setActive(true);  return map(e); }
    @Override @Transactional public RoomTypeResponse deactivate(Long id) { var e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("RoomType not found")); e.setActive(false); return map(e); }
}
