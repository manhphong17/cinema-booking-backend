package vn.cineshow.dto.request.holiday;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record HolidayRequest(
         String description,
         LocalDate holidayDate,  // ví dụ: 2025-01-01
         Boolean isRecurring   // true = hàng năm
) {
}
