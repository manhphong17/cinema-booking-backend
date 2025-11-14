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

    /**
     * GET /api/theater_details
     * Lấy thông tin cấu hình theater hiện tại
     * Requires ADMIN authority
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





}
