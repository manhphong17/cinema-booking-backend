package vn.cineshow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "holidays")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holiday extends AbstractEntity {

    @Column(nullable = false, length = 100)
    private String description; // Tên ngày lễ

    // Dùng cho ngày lễ theo năm cụ thể (vd: 2025-01-29)
    private LocalDate holidayDate;

    // Dùng cho lễ cố định hàng năm
    private Integer dayOfMonth;
    private Integer monthOfYear;

    // true = áp dụng hàng năm, false = chỉ áp dụng 1 năm cụ thể
    private boolean isRecurring;
}