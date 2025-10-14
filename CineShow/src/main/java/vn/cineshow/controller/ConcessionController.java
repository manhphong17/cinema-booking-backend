package vn.cineshow.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.ConcessionAddRequest;
import vn.cineshow.dto.request.ConcessionUpdateRequest;
import vn.cineshow.dto.response.ConcessionResponse;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.enums.ConcessionStatus;
import vn.cineshow.service.ConcessionService;

@RestController
@RequestMapping("/concession")
@RequiredArgsConstructor
public class ConcessionController {

    private final ConcessionService concessionService;

    @GetMapping
    public ResponseData<Page<ConcessionResponse>> showList(
            @RequestParam(defaultValue = "0") int page,        // trang hiện tại (mặc định 0)
            @RequestParam(defaultValue = "10") int size,       // số item / trang
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) String concessionType,
            @RequestParam(required = false) String concessionStatus,
            @RequestParam(required = false) String keyword
    ) {
        Page<ConcessionResponse> concessions = concessionService.getFilteredConcessions(
                stockStatus, concessionType, concessionStatus, keyword, page, size
        );

        if (concessions.isEmpty()) {
            return new ResponseData<>(
                    HttpStatus.NOT_FOUND.value(),
                    "Không tìm thấy sản phẩm phù hợp."
            );
        }

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Fetched concession list successfully",
                concessions
        );
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<Long> addConcession(@ModelAttribute @Valid ConcessionAddRequest concessionAddRequest) {

        Long concessionId = concessionService.addConcession(concessionAddRequest);

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Đã thêm sản phẩm thành công",
                concessionId
        );
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<ConcessionResponse> updateConcession(
            @PathVariable Long id,
            @ModelAttribute @Valid ConcessionUpdateRequest concessionUpdateRequest
    ) {
        ConcessionResponse updated = concessionService.updateConcession(id, concessionUpdateRequest);

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Cập nhật sản phẩm thành công",
                updated
        );
    }

    @PutMapping("/{id}/stock")
    public ResponseData<ConcessionResponse> addStock(
            @PathVariable Long id,
            @RequestParam int quantityToAdd
    ) {
        if (quantityToAdd <= 0) {
            return new ResponseData<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Số lượng cần thêm phải lớn hơn 0"
            );
        }

        ConcessionResponse response = concessionService.addStock(id, quantityToAdd);

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Cập nhật số lượng hàng thành công",
                response
        );
    }

    @PutMapping("/{id}/status")
    public ResponseData<ConcessionResponse> updateConcessionStatus(
            @PathVariable Long id,
            @RequestParam ConcessionStatus status
    ) {
        ConcessionResponse response = concessionService.updateConcessionStatus(id, status);

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Cập nhật trạng thái kinh doanh thành công",
                response
        );
    }

    @DeleteMapping("/{id}")
    public ResponseData<Void> deleteConcession(@PathVariable Long id) {
        concessionService.deleteConcession(id);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Đã xóa sản phẩm thành công"
        );
    }

}
