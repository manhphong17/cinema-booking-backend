package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.response.seat.seat_type.SeatTypeDTO;
import vn.cineshow.model.SeatType;
import vn.cineshow.repository.SeatTypeRepository;
import vn.cineshow.service.SeatTypeService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
}
