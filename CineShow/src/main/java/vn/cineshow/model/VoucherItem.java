package vn.cineshow.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
    private VoucherItemStatus status;

    @ManyToOne
    User user;

    @ManyToOne
    Voucher voucher;
}
