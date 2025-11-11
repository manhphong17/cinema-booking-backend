package vn.cineshow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.cineshow.dto.response.theater.BannerUploadResponse;
import vn.cineshow.exception.IllegalParameterException;
import vn.cineshow.exception.ResourceNotFoundException;
import vn.cineshow.model.Theater;
import vn.cineshow.repository.TheaterRepository;
import vn.cineshow.service.impl.S3Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TheaterBannerService {

    private final S3Service s3Service;
    private final TheaterRepository theaterRepository;

    // Cấu hình validation
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * POST /api/theater_banner
     * Upload banner image và trả về URL
     */
    @Transactional
    public BannerUploadResponse uploadBanner(MultipartFile file) throws IOException {
        // Validate file
        validateBannerFile(file);

        // Upload to S3
        String bannerUrl = s3Service.uploadFile(file);

        return BannerUploadResponse.builder()
                .bannerUrl(bannerUrl)
                .build();
    }

    /**
     * DELETE /api/theater_banner
     * Xóa banner hiện tại của theater
     */
    @Transactional
    public void deleteBanner() {
        Theater theater = theaterRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Theater configuration not found"));

        String currentBannerUrl = theater.getBannerUrl();

        // Xóa file trên S3 nếu có
        if (currentBannerUrl != null && !currentBannerUrl.isBlank()) {
            s3Service.deleteByUrl(currentBannerUrl);
        }

        // Xóa liên kết trong database
        theater.setBannerUrl(null);
        theaterRepository.save(theater);
    }

    /**
     * Validate banner file
     */
    private void validateBannerFile(MultipartFile file) throws IOException {
        // Check if file is empty
        if (file == null || file.isEmpty()) {
            throw new IllegalParameterException("Banner file is required");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalParameterException(
                    String.format("File size must not exceed %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new IllegalParameterException(
                    "Invalid file type. Only JPEG, PNG, and WebP images are allowed"
            );
        }
    }
}
