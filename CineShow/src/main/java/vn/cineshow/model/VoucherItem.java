package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import vn.cineshow.enums.VoucherItemStatus;

import java.io.Serializable;

@Entity
@Table(name = "voucher_items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class VoucherItem extends AbstractEntity implements Serializable {
    @Enumerated(EnumType.STRING)
    private VoucherItemStatus status;

    @ManyToOne
    User user;

    @ManyToOne
    Voucher voucher;
}
