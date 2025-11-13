package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.request.seat.SeatTypeCreateRequest;
import vn.cineshow.dto.request.seat.SeatTypeUpdateRequest;
import vn.cineshow.dto.response.seat.seat_type.SeatTypeDTO;
import vn.cineshow.dto.response.seat.seat_type.SeatTypeResponse;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.SeatType;
import vn.cineshow.repository.SeatRepository;
import vn.cineshow.repository.SeatTypeRepository;
import vn.cineshow.repository.ShowTimeRepository;
import vn.cineshow.service.SeatTypeService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatTypeServiceImpl implements SeatTypeService {

    private final SeatTypeRepository seatTypeRepository;
    private final SeatRepository seatRepository;
    private final ShowTimeRepository showTimeRepository;

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
        var list = seatTypeRepository.findAll();
        if (Boolean.TRUE.equals(onlyActive)) {
            list = list.stream().filter(st -> Boolean.TRUE.equals(st.getActive())).toList();
        }
        return list.stream().map(this::map).toList();
    }

    @Override @Transactional(readOnly = true)
    public SeatTypeResponse findById(Long id) {
        var e = seatTypeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND));
        return map(e);
    }

    @Override
    public SeatTypeResponse create(SeatTypeCreateRequest req) {
        if (req.getName() == null || req.getName().isBlank()) throw new AppException(ErrorCode.INVALID_PARAMETER);
        var name = req.getName().trim();
        if (seatTypeRepository.existsByNameIgnoreCase(name)) throw new AppException(ErrorCode.SEAT_TYPE_ALREADY_EXISTED);

        var e = SeatType.builder()
                .name(name)
                .description(req.getDescription())
                .active(req.getActive() == null ? true : req.getActive())
                .build();
        seatTypeRepository.save(e);
        return map(e);
    }

    @Override
    public SeatTypeResponse update(Long id, SeatTypeUpdateRequest req) {
        var e = seatTypeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND));
        if (showTimeRepository.existsBySeatTypeId(id)) {
            throw new AppException(ErrorCode.SEAT_TYPE_IN_USE);
        }
        if (req.getName() == null || req.getName().isBlank()) throw new AppException(ErrorCode.INVALID_PARAMETER);
        var newName = req.getName().trim();
        if (seatTypeRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) throw new AppException(ErrorCode.SEAT_TYPE_ALREADY_EXISTED);

        e.setName(newName);
        e.setDescription(req.getDescription());
        if (req.getActive() != null) e.setActive(req.getActive());
        return map(e);
    }

    @Override
    public SeatTypeResponse patch(Long id, SeatTypeUpdateRequest req) {
        var e = seatTypeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND));
        if (showTimeRepository.existsBySeatTypeId(id)) {
            throw new AppException(ErrorCode.SEAT_TYPE_IN_USE);
        }
        if (req.getName() != null) {
            var newName = req.getName().trim();
            if (newName.isBlank()) throw new AppException(ErrorCode.INVALID_PARAMETER);
            if (seatTypeRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) throw new AppException(ErrorCode.SEAT_TYPE_ALREADY_EXISTED);
            e.setName(newName);
        }
        if (req.getDescription() != null) e.setDescription(req.getDescription());
        if (req.getActive() != null) e.setActive(req.getActive());
        return map(e);
    }

    @Override public void delete(Long id) {
        if (!seatTypeRepository.existsById(id)) {
            throw new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND);
        }
        if (showTimeRepository.existsBySeatTypeId(id)) {
            throw new AppException(ErrorCode.SEAT_TYPE_IN_USE);
        }
        if (!seatRepository.findBySeatType_Id(id).isEmpty()) {
            throw new AppException(ErrorCode.SEAT_TYPE_IN_USE);
        }
        seatTypeRepository.deleteById(id);
    }

    @Override public SeatTypeResponse activate(Long id)   { var e = seatTypeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND)); e.setActive(true);  return map(e); }
    @Override public SeatTypeResponse deactivate(Long id) {
        if (showTimeRepository.existsBySeatTypeId(id)) {
            throw new AppException(ErrorCode.SEAT_TYPE_IN_USE);
        }
        var e = seatTypeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND)); e.setActive(false); return map(e); }
}
