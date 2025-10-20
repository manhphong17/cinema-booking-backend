package vn.cineshow.service.impl;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.request.concession.ConcessionAddRequest;
import vn.cineshow.dto.request.concession.ConcessionUpdateRequest;
import vn.cineshow.dto.response.concession.ConcessionResponse;
import vn.cineshow.dto.response.concession.ConcessionTypeResponse;
import vn.cineshow.enums.ConcessionStatus;
import vn.cineshow.enums.ConcessionTypeStatus;
import vn.cineshow.enums.StockStatus;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.Concession;
import vn.cineshow.model.ConcessionType;
import vn.cineshow.repository.ConcessionRepository;
import vn.cineshow.repository.ConcessionTypeRepository;
import vn.cineshow.service.ConcessionService;

import java.io.IOException;


@Service
@RequiredArgsConstructor
@Slf4j(topic = "")
class ConcessionServiceImpl implements ConcessionService {

    private final ConcessionRepository concessionRepository;
    private final ConcessionTypeRepository concessionTypeRepository;
    private final S3Service s3Service;

    @Override
    public Page<ConcessionResponse> getFilteredConcessions(
            String stockStatus,
            Long concessionTypeId,
            String concessionStatus,
            String keyword,
            int page,
            int size
    ) {
        //  1. Convert string → enum (nếu khác ALL)
        StockStatus stock = null;
        ConcessionType type = null;
        ConcessionStatus status = null;

        try {
            if (stockStatus != null && !stockStatus.equalsIgnoreCase("ALL"))
                stock = StockStatus.valueOf(stockStatus.toUpperCase());
            if (concessionStatus != null && !concessionStatus.equalsIgnoreCase("ALL"))
                status = ConcessionStatus.valueOf(concessionStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid filter value provided: {}", e.getMessage());
        }

        //  2. Tạo Pageable
        Pageable pageable = PageRequest.of(page, size);

        // 3. Gọi repo để lấy page kết quả
        Page<Concession> pageResult =
                concessionRepository.findFilteredConcessions(stock, concessionTypeId, status, keyword, pageable);

        //  4. Map sang Page<ConcessionResponse>
        return pageResult.map(c -> new ConcessionResponse(
                c.getId(),
                c.getName(),
                c.getPrice(),
                c.getDescription(),
                ConcessionTypeResponse.builder()
                        .id(c.getConcessionType().getId())
                        .name(c.getConcessionType().getName())
                        .status(c.getConcessionType().getStatus().name())
                        .build(),
                c.getUnitInStock(),
                c.getStockStatus(),
                c.getConcessionStatus(),
                c.getUrlImage()
        ));
    }


    @Override
    public Long addConcession(ConcessionAddRequest concessionAddRequest) {

        String urlImage = null;

        try {
            if (concessionAddRequest.file() != null || !concessionAddRequest.file().isEmpty()) {
                urlImage = s3Service.uploadFile(concessionAddRequest.file());
            }
        } catch (IOException e) {
            // Wrap exception gốc thành AppException
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        ConcessionType concessionType =
                concessionTypeRepository.findByIdAndStatusNot(
                        concessionAddRequest.concessionTypeId(),
                        ConcessionTypeStatus.DELETED
                );

        Concession concession = new Concession();
        concession.setName(concessionAddRequest.name());
        concession.setPrice(concessionAddRequest.price());
        concession.setDescription(concessionAddRequest.description());
        concession.setConcessionType(concessionType);
        concession.setUnitInStock(concessionAddRequest.unitInStock());
        concession.setStockStatus(StockStatus.IN_STOCK);
        concession.setConcessionStatus(ConcessionStatus.ACTIVE);
        concession.setUrlImage(urlImage);
        concessionRepository.save(concession);
        return concession.getId();
    }

    @Override
    public ConcessionResponse updateConcession(Long id, ConcessionUpdateRequest request) {
        //  1. Tìm sản phẩm theo ID
        Concession concession = concessionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONCESSION_NOT_FOUND));

        //  2. Cập nhật các thông tin cơ bản
        ConcessionType concessionType =
                concessionTypeRepository.findByIdAndStatusNot(
                        request.concessionTypeId(),
                        ConcessionTypeStatus.DELETED
                );

        concession.setName(request.name());
        concession.setPrice(request.price());
        concession.setDescription(request.description());
        concession.setConcessionType(concessionType);
        concession.setUnitInStock(request.unitInStock());

        //  3. Upload ảnh mới nếu có
        if (request.file() != null && !request.file().isEmpty()) {
            try {

                // Upload ảnh mới
                String newUrl = s3Service.uploadFile(request.file());
                concession.setUrlImage(newUrl);
            } catch (IOException e) {
                log.error(" Lỗi upload ảnh khi cập nhật concession: {}", e.getMessage());
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }

        //  4. Giữ nguyên status hiện tại (không đổi ACTIVE/INACTIVE khi chỉ update)

        //  5. Cập nhật lại stockStatus nếu cần (IN_STOCK hoặc SOLD_OUT)
        if (concession.getUnitInStock() > 0) {
            concession.setStockStatus(StockStatus.IN_STOCK);
        } else {
            concession.setStockStatus(StockStatus.SOLD_OUT);
        }

        // 6. Lưu và trả về DTO
        Concession saved = concessionRepository.save(concession);


        return new ConcessionResponse(
                saved.getId(),
                saved.getName(),
                saved.getPrice(),
                saved.getDescription(),
                ConcessionTypeResponse.builder()
                        .id(saved.getConcessionType().getId())
                        .name(saved.getConcessionType().getName())
                        .status(saved.getConcessionType().getStatus().name())
                        .build(),
                saved.getUnitInStock(),
                saved.getStockStatus(),
                saved.getConcessionStatus(),
                saved.getUrlImage()
        );
    }

    @Override
    public ConcessionResponse addStock(Long id, int quantityToAdd) {
        Concession concession = concessionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONCESSION_NOT_FOUND));

        if (quantityToAdd < 1 || quantityToAdd > 999) {
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }

        concession.setUnitInStock(concession.getUnitInStock() + quantityToAdd);

        if (concession.getStockStatus() == StockStatus.SOLD_OUT && concession.getUnitInStock() > 0) {
            concession.setStockStatus(StockStatus.IN_STOCK);
        }

        concessionRepository.save(concession);

        return new ConcessionResponse(
                concession.getId(),
                concession.getName(),
                concession.getPrice(),
                concession.getDescription(),
                ConcessionTypeResponse.builder()
                        .id(concession.getConcessionType().getId())
                        .name(concession.getConcessionType().getName())
                        .status(concession.getConcessionType().getStatus().name())
                        .build(),
                concession.getUnitInStock(),
                concession.getStockStatus(),
                concession.getConcessionStatus(),
                concession.getUrlImage()
        );
    }

    @Override
    public ConcessionResponse updateConcessionStatus(Long id, ConcessionStatus status) {
        Concession concession = concessionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONCESSION_NOT_FOUND));

        concession.setConcessionStatus(status);
        concessionRepository.save(concession);

        return new ConcessionResponse(
                concession.getId(),
                concession.getName(),
                concession.getPrice(),
                concession.getDescription(),
                ConcessionTypeResponse.builder()
                        .id(concession.getConcessionType().getId())
                        .name(concession.getConcessionType().getName())
                        .status(concession.getConcessionType().getStatus().name())
                        .build(),
                concession.getUnitInStock(),
                concession.getStockStatus(),
                concession.getConcessionStatus(),
                concession.getUrlImage()
        );
    }

    @Override
    @Transactional
    public void deleteConcession(Long id) {
        Concession concession = concessionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONCESSION_NOT_FOUND));

        if (concession.getConcessionStatus() == ConcessionStatus.DELETED) {
            throw new AppException(ErrorCode.CONCESSION_ALREADY_DELETED);
        }

        //  Xóa ảnh trên S3 (nếu có)
        s3Service.deleteByUrl(concession.getUrlImage());

        // Soft delete
        concession.setConcessionStatus(ConcessionStatus.DELETED);
        concessionRepository.save(concession);
    }


}
