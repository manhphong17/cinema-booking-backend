package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.response.room.room_type.RoomTypeDTO;
import vn.cineshow.model.RoomType;
import vn.cineshow.repository.RoomTypeRepository;
import vn.cineshow.service.RoomTypeService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
}
