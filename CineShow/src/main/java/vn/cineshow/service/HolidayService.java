package vn.cineshow.service;


import org.springframework.data.domain.Page;
import vn.cineshow.dto.request.holiday.HolidayRequest;
import vn.cineshow.dto.response.holiday.HolidayResponse;
import vn.cineshow.model.Holiday;

import java.time.LocalDate;
import java.util.List;

public interface HolidayService {
    List<Holiday> addHolidays(List<HolidayRequest> requests);
    Page<HolidayResponse> getHolidays(String filterType, int page, int limit, Integer year);
    public void deleteHolidayById(Long id);

    boolean isHoliday(LocalDate date);
}
