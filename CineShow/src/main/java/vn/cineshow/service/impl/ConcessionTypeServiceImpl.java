package vn.cineshow.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.response.concession.ConcessionTypeResponse;
import vn.cineshow.enums.ConcessionTypeStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.ConcessionType;
import vn.cineshow.repository.ConcessionRepository;
import vn.cineshow.repository.ConcessionTypeRepository;
import vn.cineshow.service.ConcessionTypeService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
class ConcessionTypeServiceImpl implements ConcessionTypeService {

    private final ConcessionTypeRepository concessionTypeRepo;
    private final ConcessionRepository concessionRepo;

    @Override
    public List<ConcessionTypeResponse> getAll() {
        List<ConcessionType> types = concessionTypeRepo.findAllByStatus(ConcessionTypeStatus.ACTIVE);

        return types.stream()
                .map(t -> new ConcessionTypeResponse(
                        t.getId(),
                        t.getName(),
                        t.getStatus().name()
                ))
                .collect(Collectors.toList());
    }


    @Override
    public void updateStatus(Long id) {
        ConcessionType type = concessionTypeRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONCESSION_TYPE_NOT_FOUND));

        if (type.getStatus() == ConcessionTypeStatus.ACTIVE) {
            long count = concessionRepo.countByConcessionType_Id(id);
            if (count > 0) {
                throw new AppException(ErrorCode.CONCESSION_TYPE_IN_USE);
            }
        }

        type.setStatus(ConcessionTypeStatus.DELETED);
        concessionTypeRepo.save(type);
    }

    @Override
    public void addConcessionType(String name) {
        String trimmedName = name.trim();

        // Tìm loại theo tên (không phân biệt hoa thường)
        Optional<ConcessionType> existing = concessionTypeRepo.findByNameIgnoreCase(trimmedName);

        if (existing.isPresent()) {
            ConcessionType type = existing.get();

            // Nếu đang ACTIVE → lỗi trùng
            if (type.getStatus() == ConcessionTypeStatus.ACTIVE) {
                throw new AppException(ErrorCode.CONCESSION_TYPE_EXISTED);
            }

            // Nếu đang DELETED → kích hoạt lại
            type.setStatus(ConcessionTypeStatus.ACTIVE);
            concessionTypeRepo.save(type);
            return;
        }

        // Nếu chưa tồn tại → tạo mới
        ConcessionType newType = ConcessionType.builder()
                .name(trimmedName)
                .status(ConcessionTypeStatus.ACTIVE)
                .build();

        concessionTypeRepo.save(newType);
    }

}


