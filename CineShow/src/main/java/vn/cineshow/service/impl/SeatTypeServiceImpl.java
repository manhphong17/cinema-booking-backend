package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.request.seat.SeatTypeCreateRequest;
import vn.cineshow.dto.request.seat.SeatTypeUpdateRequest;
import vn.cineshow.dto.response.seat.seat_type.SeatTypeDTO;
import vn.cineshow.dto.response.seat.seat_type.SeatTypeResponse;
import vn.cineshow.model.SeatType;
import vn.cineshow.repository.SeatTypeRepository;
import vn.cineshow.service.SeatTypeService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatTypeServiceImpl implements SeatTypeService {

    private final SeatTypeRepository seatTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SeatTypeDTO> getAllSeatTypesDTO() {
        // Sắp xếp theo name để UI hiển thị ổn định; có thể đổi field khác nếu cần
        List<SeatType> entities = seatTypeRepository.findAll(Sort.by(Sort.Order.asc("name")));
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /* =========================
       Helpers
       ========================= */
    private SeatTypeDTO toDTO(SeatType st) {
        return SeatTypeDTO.builder()
                .id(st.getId())
//                .code(st.getCode())
                .name(st.getName())
                .description(st.getDescription())
                .build();
    }

    private final SeatTypeRepository repo;

    private SeatTypeResponse map(SeatType e) {
        return SeatTypeResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .active(Boolean.TRUE.equals(e.getActive()))
                .build();
    }

    @Override @Transactional(readOnly = true)
    public List<SeatTypeResponse> findAll(Boolean onlyActive) {
        var list = repo.findAll();
        if (Boolean.TRUE.equals(onlyActive)) {
            list = list.stream().filter(st -> Boolean.TRUE.equals(st.getActive())).toList();
        }
        return list.stream().map(this::map).toList();
    }

    @Override @Transactional(readOnly = true)
    public SeatTypeResponse findById(Long id) {
        var e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("SeatType not found"));
        return map(e);
    }

    @Override
    public SeatTypeResponse create(SeatTypeCreateRequest req) {
        if (req.getName() == null || req.getName().isBlank()) throw new IllegalArgumentException("Name is required");
        var name = req.getName().trim();
        if (repo.existsByNameIgnoreCase(name)) throw new IllegalArgumentException("Name already exists");

        var e = SeatType.builder()
                .name(name)
                .description(req.getDescription())
                .active(req.getActive() == null ? true : req.getActive())
                .build();
        repo.save(e);
        return map(e);
    }

    @Override
    public SeatTypeResponse update(Long id, SeatTypeUpdateRequest req) {
        var e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("SeatType not found"));
        if (req.getName() == null || req.getName().isBlank()) throw new IllegalArgumentException("Name is required");
        var newName = req.getName().trim();
        if (repo.existsByNameIgnoreCaseAndIdNot(newName, id)) throw new IllegalArgumentException("Name already exists");

        e.setName(newName);
        e.setDescription(req.getDescription());
        if (req.getActive() != null) e.setActive(req.getActive());
        return map(e);
    }

    @Override
    public SeatTypeResponse patch(Long id, SeatTypeUpdateRequest req) {
        var e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("SeatType not found"));
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

    @Override public void delete(Long id) { repo.deleteById(id); }

    @Override public SeatTypeResponse activate(Long id)   { var e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("SeatType not found")); e.setActive(true);  return map(e); }
    @Override public SeatTypeResponse deactivate(Long id) { var e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("SeatType not found")); e.setActive(false); return map(e); }
}