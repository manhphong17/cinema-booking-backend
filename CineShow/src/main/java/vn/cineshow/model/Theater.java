package vn.cineshow.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "theater")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Theater {

    @Id
    @Column(name = "id")
    Long id; // = 1L (single row)

    // ==== Thông tin cơ bản ====
    @Column(name = "name", nullable = false, length = 100)
    String name;

    @Column(name = "address", nullable = false, length = 255)
    String address;

    @Column(name = "hotline", nullable = false, length = 30)
    String hotline;

    @Column(name = "contact_email", nullable = false, length = 120)
    String contactEmail;

    @Column(name = "google_map_url", length = 512)
    String googleMapUrl;

    // ==== Giờ hoạt động ====
    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "open_time", nullable = false)
    LocalTime openTime;

    @JsonFormat(pattern = "HH:mm:ss")
    @Column(name = "close_time", nullable = false)
    LocalTime closeTime;

    @Column(name = "overnight", nullable = false)
    Boolean overnight = Boolean.FALSE;


    // ==== Thông tin ====
    @Column(name = "information", columnDefinition = "TEXT")
    String information;

    // ==== Audit cơ bản (nếu bạn có AbstractEntity thì thay thế) ====
    @Column(name = "created_by", length = 60)
    String createdBy;

    @Column(name = "updated_by", length = 60)
    String updatedBy;

    // ==== Lịch sử thay đổi ====
    @OneToMany(mappedBy = "theater", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<TheaterUpdateHistory> updateHistories;
}
