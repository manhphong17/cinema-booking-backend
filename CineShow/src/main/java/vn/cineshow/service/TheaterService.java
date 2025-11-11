package vn.cineshow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.request.theater.TheaterRequest;
import vn.cineshow.dto.response.theater.TheaterResponse;
import vn.cineshow.exception.IllegalParameterException;
import vn.cineshow.exception.ResourceNotFoundException;
import vn.cineshow.model.Theater;
import vn.cineshow.repository.TheaterRepository;

import java.time.LocalTime;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final TheaterUpdateHistoryService historyService;

    // Regex patterns for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{10,15}$"
    );
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$"
    );

    /**
     * GET /api/theater_details
     * Lấy thông tin cấu hình theater (single row, id = 1)
     */
    public TheaterResponse getTheaterDetails() {
        Theater theater = theaterRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Theater configuration not found"));

        return mapToResponse(theater);
    }

    /**
     * PUT /api/theater_details
     * Cập nhật thông tin theater
     */
    @Transactional
    public TheaterResponse updateTheaterDetails(TheaterRequest request) {
        // Validate input
        validateTheaterRequest(request);

        // Get existing theater
        Theater theater = theaterRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Theater configuration not found"));

        // Log changes before updating
        String updatedBy = request.getUpdatedBy() != null ? request.getUpdatedBy() : "system";
        logFieldChange(theater.getId(), "name", theater.getName(), request.getName(), updatedBy);
        logFieldChange(theater.getId(), "address", theater.getAddress(), request.getAddress(), updatedBy);
        logFieldChange(theater.getId(), "hotline", theater.getHotline(), request.getHotline(), updatedBy);
        logFieldChange(theater.getId(), "contactEmail", theater.getContactEmail(), request.getContactEmail(), updatedBy);
        logFieldChange(theater.getId(), "googleMapUrl", theater.getGoogleMapUrl(), request.getGoogleMapUrl(), updatedBy);
        logFieldChange(theater.getId(), "openTime", theater.getOpenTime(), request.getOpenTime(), updatedBy);
        logFieldChange(theater.getId(), "closeTime", theater.getCloseTime(), request.getCloseTime(), updatedBy);
        logFieldChange(theater.getId(), "overnight", theater.getOvernight(), Boolean.TRUE.equals(request.getOvernight()), updatedBy);
        logFieldChange(theater.getId(), "bannerUrl", theater.getBannerUrl(), request.getBannerUrl(), updatedBy);
        logFieldChange(theater.getId(), "information", theater.getInformation(), request.getInformation(), updatedBy);
        logFieldChange(theater.getId(), "representativeName", theater.getRepresentativeName(), request.getRepresentativeName(), updatedBy);
        logFieldChange(theater.getId(), "representativeTitle", theater.getRepresentativeTitle(), request.getRepresentativeTitle(), updatedBy);
        logFieldChange(theater.getId(), "representativePhone", theater.getRepresentativePhone(), request.getRepresentativePhone(), updatedBy);
        logFieldChange(theater.getId(), "representativeEmail", theater.getRepresentativeEmail(), request.getRepresentativeEmail(), updatedBy);

        // Update fields
        theater.setName(request.getName());
        theater.setAddress(request.getAddress());
        theater.setHotline(request.getHotline());
        theater.setContactEmail(request.getContactEmail());
        theater.setGoogleMapUrl(request.getGoogleMapUrl());
        theater.setOpenTime(request.getOpenTime());
        theater.setCloseTime(request.getCloseTime());
        theater.setOvernight(Boolean.TRUE.equals(request.getOvernight()));
        theater.setBannerUrl(request.getBannerUrl());
        theater.setInformation(request.getInformation());
        theater.setRepresentativeName(request.getRepresentativeName());
        theater.setRepresentativeTitle(request.getRepresentativeTitle());
        theater.setRepresentativePhone(request.getRepresentativePhone());
        theater.setRepresentativeEmail(request.getRepresentativeEmail());
        theater.setUpdatedBy(request.getUpdatedBy());

        // Save and return
        Theater saved = theaterRepository.save(theater);
        return mapToResponse(saved);
    }

    /**
     * Log field change to history
     */
    private void logFieldChange(Long theaterId, String fieldName, Object oldValue, Object newValue, String updatedBy) {
        historyService.logChange(theaterId, fieldName, oldValue, newValue, updatedBy);
    }

    /**
     * Validate theater request
     */
    private void validateTheaterRequest(TheaterRequest request) {
        // Required fields
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalParameterException("Theater name is required");
        }
        if (request.getAddress() == null || request.getAddress().isBlank()) {
            throw new IllegalParameterException("Address is required");
        }
        if (request.getHotline() == null || request.getHotline().isBlank()) {
            throw new IllegalParameterException("Hotline is required");
        }
        if (request.getContactEmail() == null || request.getContactEmail().isBlank()) {
            throw new IllegalParameterException("Contact email is required");
        }
        if (request.getOpenTime() == null) {
            throw new IllegalParameterException("Open time is required");
        }
        if (request.getCloseTime() == null) {
            throw new IllegalParameterException("Close time is required");
        }
        if (request.getRepresentativeName() == null || request.getRepresentativeName().isBlank()) {
            throw new IllegalParameterException("Representative name is required");
        }
        if (request.getRepresentativePhone() == null || request.getRepresentativePhone().isBlank()) {
            throw new IllegalParameterException("Representative phone is required");
        }
        if (request.getRepresentativeEmail() == null || request.getRepresentativeEmail().isBlank()) {
            throw new IllegalParameterException("Representative email is required");
        }

        // Validate email format
        if (!EMAIL_PATTERN.matcher(request.getContactEmail()).matches()) {
            throw new IllegalParameterException("Invalid contact email format");
        }
        if (!EMAIL_PATTERN.matcher(request.getRepresentativeEmail()).matches()) {
            throw new IllegalParameterException("Invalid representative email format");
        }

        // Validate phone format
        if (!PHONE_PATTERN.matcher(request.getHotline().replaceAll("[\\s-]", "")).matches()) {
            throw new IllegalParameterException("Invalid hotline format");
        }
        if (!PHONE_PATTERN.matcher(request.getRepresentativePhone().replaceAll("[\\s-]", "")).matches()) {
            throw new IllegalParameterException("Invalid representative phone format");
        }



        // Validate open/close time logic
        validateOperatingHours(request.getOpenTime(), request.getCloseTime(), request.getOvernight());
    }

    /**
     * Validate operating hours
     */
    private void validateOperatingHours(LocalTime openTime, LocalTime closeTime, Boolean overnight) {
        if (overnight != null && overnight) {
            // If overnight is true, closeTime should be before openTime
            if (!closeTime.isBefore(openTime)) {
                throw new IllegalParameterException(
                        "When overnight mode is enabled, close time must be before open time"
                );
            }
        } else {
            // Normal mode: closeTime should be after openTime
            if (!closeTime.isAfter(openTime)) {
                throw new IllegalParameterException(
                        "Close time must be after open time"
                );
            }
        }
    }

    /**
     * Map Theater entity to TheaterResponse
     */
    private TheaterResponse mapToResponse(Theater theater) {
        return TheaterResponse.builder()
                .id(theater.getId())
                .name(theater.getName())
                .address(theater.getAddress())
                .hotline(theater.getHotline())
                .contactEmail(theater.getContactEmail())
                .googleMapUrl(theater.getGoogleMapUrl())
                .openTime(theater.getOpenTime())
                .closeTime(theater.getCloseTime())
                .overnight(theater.getOvernight())
                .bannerUrl(theater.getBannerUrl())
                .information(theater.getInformation())
                .representativeName(theater.getRepresentativeName())
                .representativeTitle(theater.getRepresentativeTitle())
                .representativePhone(theater.getRepresentativePhone())
                .representativeEmail(theater.getRepresentativeEmail())
                .createdBy(theater.getCreatedBy())
                .updatedBy(theater.getUpdatedBy())
                .build();
    }
}
