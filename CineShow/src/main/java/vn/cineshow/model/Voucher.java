package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vouchers")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Voucher extends AbstractEntity implements Serializable {

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    String code;

    int discount;

    @Column(length = 255)
    String description;

    int maxUsage;

    int currentUsage;

    LocalDate startDate;

    LocalDate endDate;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL)
    private List<VoucherItem> voucherItems;


    @Column(nullable = false)
    boolean isActive =false;

    boolean isDeleted =false;

}
