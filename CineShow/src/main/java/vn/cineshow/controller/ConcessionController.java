package vn.cineshow.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.cineshow.dto.request.concession.ConcessionAddRequest;
import vn.cineshow.dto.request.concession.ConcessionTypeRequest;
import vn.cineshow.dto.request.concession.ConcessionUpdateRequest;
import vn.cineshow.dto.response.concession.ConcessionResponse;
import vn.cineshow.dto.response.concession.ConcessionSimpleResponse;
import vn.cineshow.dto.response.concession.ConcessionTypeResponse;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.enums.ConcessionStatus;
import vn.cineshow.service.ConcessionService;
import vn.cineshow.service.ConcessionTypeService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/concession")
@RequiredArgsConstructor
public class ConcessionController {

    private final ConcessionService concessionService;
    private final ConcessionTypeService concessionTypeService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('BUSINESS','CUSTOMER')")
    public ResponseData<Page<ConcessionResponse>> showListConcessions(
            @RequestParam(defaultValue = "0") int page,        // trang hiện tại (mặc định 0)
            @RequestParam(defaultValue = "10") int size,       // số item / trang
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) Long concessionTypeId,
            @RequestParam(required = false) String concessionStatus,
            @RequestParam(required = false) String keyword
    ) {
        Page<ConcessionResponse> concessions = concessionService.getFilteredConcessions(
                stockStatus, concessionTypeId, concessionStatus, keyword, page, size
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


    @PreAuthorize("hasAuthority('BUSINESS')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<Long> addConcession(@ModelAttribute @Valid ConcessionAddRequest concessionAddRequest) {

        Long concessionId = concessionService.addConcession(concessionAddRequest);

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Đã thêm sản phẩm thành công",
                concessionId
        );
    }

    @PreAuthorize("hasAuthority('BUSINESS')")
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

    @PreAuthorize("hasAuthority('BUSINESS')")
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

    @PreAuthorize("hasAuthority('BUSINESS')")
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

    @PreAuthorize("hasAuthority('BUSINESS')")
    @DeleteMapping("/{id}")
    public ResponseData<Void> deleteConcession(@PathVariable Long id) {
        concessionService.deleteConcession(id);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Đã xóa sản phẩm thành công"
        );
    }

    // TYPE
    @GetMapping("/types")
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseData<List<ConcessionTypeResponse>> getAllConcessionTypes() {
        List<ConcessionTypeResponse> types = concessionTypeService.getAll();

        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Fetched all concession types successfully.",
                types
        );
    }

    @PutMapping("/types/{id}/status")
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseData<Void> updateConcessionTypeStatus(@PathVariable Long id) {
        concessionTypeService.updateStatus(id);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Cập nhật trạng thái loại sản phẩm thành công."
        );
    }

    @PostMapping("/type")
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseData<Void> addConcessionType(@Valid @RequestBody ConcessionTypeRequest request) {
        concessionTypeService.addConcessionType(request.getName());
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Thêm loại sản phẩm mới thành công."
        );
    }

    @GetMapping("/list-by-ids")
    public ResponseData<List<ConcessionSimpleResponse>> getConcessionsByIds(@RequestParam("ids") String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());

        List<ConcessionSimpleResponse> result = concessionService.getConcessionsByIds(idList);
        return new ResponseData<>(HttpStatus.OK.value(), "Get concession list successfully", result);
    }
}


