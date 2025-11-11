package vn.cineshow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.response.theater.TheaterUpdateHistoryResponse;
import vn.cineshow.model.TheaterUpdateHistory;
import vn.cineshow.repository.TheaterUpdateHistoryRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class TheaterUpdateHistoryService {

    private final TheaterUpdateHistoryRepository historyRepository;

    /**
     * Ghi lại lịch sử thay đổi
     */
    @Transactional
    public void logChange(Long theaterId, String fieldName, Object oldValue, Object newValue, String updatedBy) {
        // Chỉ log nếu có thay đổi thực sự
        if (Objects.equals(oldValue, newValue)) {
            return;
        }

        TheaterUpdateHistory history = TheaterUpdateHistory.builder()
                .theaterId(theaterId)
                .updatedField(fieldName)
                .oldValue(convertToString(oldValue))
                .newValue(convertToString(newValue))
                .updatedBy(updatedBy)
                .build();

        historyRepository.save(history);
        log.info("Logged theater update: field={}, oldValue={}, newValue={}, updatedBy={}", 
                fieldName, oldValue, newValue, updatedBy);
    }

    /**
     * Lấy lịch sử thay đổi với phân trang
     */
    public Page<TheaterUpdateHistoryResponse> getHistory(Long theaterId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<TheaterUpdateHistory> historyPage = historyRepository
                .findByTheaterIdOrderByUpdatedAtDesc(theaterId, pageable);

        return historyPage.map(this::mapToResponse);
    }

    /**
     * Lấy lịch sử trong khoảng thời gian
     */
    public Page<TheaterUpdateHistoryResponse> getHistoryByDateRange(
            Long theaterId, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            int page, 
            int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<TheaterUpdateHistory> historyPage = historyRepository
                .findByTheaterIdAndUpdatedAtBetweenOrderByUpdatedAtDesc(
                        theaterId, startDate, endDate, pageable
                );

        return historyPage.map(this::mapToResponse);
    }

    /**
     * Convert object to string for storage
     */
    private String convertToString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalTime) {
            return ((LocalTime) value).toString();
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        return value.toString();
    }

    /**
     * Map entity to response DTO
     */
    private TheaterUpdateHistoryResponse mapToResponse(TheaterUpdateHistory history) {
        return TheaterUpdateHistoryResponse.builder()
                .id(history.getId())
                .theaterId(history.getTheaterId())
                .updatedField(history.getUpdatedField())
                .oldValue(history.getOldValue())
                .newValue(history.getNewValue())
                .updatedBy(history.getUpdatedBy())
                .updatedAt(history.getUpdatedAt())
                .build();
    }
}
