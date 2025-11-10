package vn.cineshow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.cineshow.dto.request.theater.TheaterRequest;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.dto.response.theater.BannerUploadResponse;
import vn.cineshow.dto.response.theater.TheaterResponse;
import vn.cineshow.service.TheaterBannerService;
import vn.cineshow.service.TheaterService;

import java.io.IOException;

/**
 * Quản lý các API đọc/cập nhật thông tin cấu hình rạp CineShow.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TheaterController {

    private final TheaterService theaterService;
    private final TheaterBannerService theaterBannerService;

    /**
     * GET /api/theater_details
     * Lấy thông tin cấu hình theater hiện tại
     * Public endpoint - có thể truy cập từ trang home
     */
    @GetMapping("/theater_details")
    public ResponseData<TheaterResponse> getTheaterDetails() {
        TheaterResponse response = theaterService.getTheaterDetails();
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Get theater details successfully",
                response
        );
    }

    /**
     * PUT /api/theater_details
     * Cập nhật thông tin theater (Admin only)
     */
    @PutMapping("/theater_details")
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseData<TheaterResponse> updateTheaterDetails(
            @Valid @RequestBody TheaterRequest request
    ) {
        TheaterResponse response = theaterService.updateTheaterDetails(request);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Theater details updated successfully",
                response
        );
    }

    /**
     * POST /api/theater_banner
     * Upload banner image (Admin only)
     * Kiểm tra MIME type, size, và aspect ratio
     */
    @PostMapping(value = "/theater_banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseData<BannerUploadResponse> uploadBanner(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        BannerUploadResponse response = theaterBannerService.uploadBanner(file);
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Banner uploaded successfully",
                response
        );
    }

    /**
     * DELETE /api/theater_banner
     * Xóa banner hiện tại (Admin only)
     * Xóa file trên S3 và gỡ liên kết trong database
     */
    @DeleteMapping("/theater_banner")
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseData<Void> deleteBanner() {
        theaterBannerService.deleteBanner();
        return new ResponseData<>(
                HttpStatus.OK.value(),
                "Banner deleted successfully",
                null
        );
    }

}
