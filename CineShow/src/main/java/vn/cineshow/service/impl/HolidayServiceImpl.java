package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.request.holiday.HolidayRequest;
import vn.cineshow.dto.response.holiday.HolidayResponse;
import vn.cineshow.exception.AppException;
import vn.cineshow.exception.ErrorCode;
import vn.cineshow.model.Holiday;
import vn.cineshow.repository.HolidayRepository;
import vn.cineshow.service.HolidayService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;

    @Override
    public List<Holiday> addHolidays(List<HolidayRequest> requests) {
        // Map từng request -> entity theo đúng rule
        List<Holiday> holidays = requests.stream()
                .map(this::toEntityWithValidation)
                .collect(Collectors.toList());

        return holidayRepository.saveAll(holidays);
    }

    @Override
    public Page<HolidayResponse> getHolidays(String filterType, int page, int limit, Integer year) {
        int pageIndex = Math.max(page - 1, 0);
        PageRequest pageable = PageRequest.of(pageIndex, limit);

        if ("recurring".equalsIgnoreCase(filterType)) {
            return holidayRepository.findByIsRecurringTrue(pageable)
                    .map(h -> new HolidayResponse(
                            h.getId(),
                            h.getDescription(),
                            String.format("%02d-%02d", h.getMonthOfYear(), h.getDayOfMonth()),
                            true
                    ));
        }

        if ("yearly".equalsIgnoreCase(filterType)) {
            // 🆕 Nếu FE có gửi year thì dùng, nếu không thì mặc định là năm hiện tại
            int targetYear = (year != null) ? year : LocalDate.now().getYear();

            return holidayRepository.findYearlyHolidays(targetYear, pageable)
                    .map(h -> new HolidayResponse(
                            h.getId(),
                            h.getDescription(),
                            h.getHolidayDate() != null ? h.getHolidayDate().toString() : "",
                            false
                    ));

        }

        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }

    @Override
    public void deleteHolidayById(Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HOLIDAY_NOT_FOUND));
        holidayRepository.delete(holiday);
    }

    /**
     * Chuyển HolidayRequest thành Holiday theo rule:
     * - isRecurring = true  -> set dayOfMonth/monthOfYear, isRecurring=true, holidayDate=null
     * - isRecurring = false -> set holidayDate, isRecurring=false, dayOfMonth/monthOfYear=null
     * Đồng thời kiểm tra trùng dữ liệu trước khi lưu.
     */
    private Holiday toEntityWithValidation(HolidayRequest req) {
        LocalDate date = req.holidayDate();
        if (date == null) {
            throw new IllegalArgumentException("holidayDate is required");
        }
        boolean recurring = Boolean.TRUE.equals(req.isRecurring());

        if (recurring) {
            int day = date.getDayOfMonth();
            int month = date.getMonthValue();

            // Check trùng ngày lễ hằng năm
            boolean exists = holidayRepository
                    .existsByDayOfMonthAndMonthOfYearAndIsRecurringTrue(day, month);
            if (exists) {
                throw new AppException(ErrorCode.HOLIDAY_EXISTED);

            }

            return Holiday.builder()
                    .description(req.description())
                    .holidayDate(null)           // hằng năm -> không dùng holidayDate
                    .dayOfMonth(day)
                    .monthOfYear(month)
                    .isRecurring(true)
                    .build();

        } else {
            // Check trùng ngày lễ theo năm cụ thể
            boolean exists = holidayRepository.existsByHolidayDate(date);
            if (exists) {
                throw new AppException(ErrorCode.HOLIDAY_EXISTED);

            }

            return Holiday.builder()
                    .description(req.description()) // record: req.description()
                    .holidayDate(date)
                    .dayOfMonth(null)
                    .monthOfYear(null)
                    .isRecurring(false)
                    .build();
        }
    }
}
