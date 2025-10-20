package vn.cineshow.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.cineshow.model.Holiday;

import java.time.LocalDate;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    boolean existsByHolidayDate(LocalDate holidayDate);
    boolean existsByDayOfMonthAndMonthOfYearAndIsRecurringTrue(int dayOfMonth, int monthOfYear);

    // Lấy danh sách ngày lễ cố định (isRecurring = true)
    Page<Holiday> findByIsRecurringTrue(Pageable pageable);

    // Lấy danh sách ngày lễ riêng của năm hiện tại
    @Query("SELECT h FROM Holiday h WHERE h.isRecurring = false AND YEAR(h.holidayDate) = :year")
    Page<Holiday> findYearlyHolidays(@Param("year")int year, Pageable pageable);
}
