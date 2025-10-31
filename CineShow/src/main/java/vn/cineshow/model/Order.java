package vn.cineshow.model;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.OrderStatus;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order extends AbstractEntity implements Serializable {

    @ManyToOne
    User user;

    @Column(columnDefinition = "decimal(10,2) DEFAULT 0.00")
    Double totalPrice;

    @Enumerated(EnumType.STRING)
    OrderStatus orderStatus; //PENDING, COMPLETED, CANCELED

    @Column(nullable = false, unique = true)
    String code;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "order")
    private List<Payment> payments;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "order")
    private List<OrderConcession> orderConcession;

    @ManyToOne(fetch = FetchType.LAZY)
    Voucher voucher;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "order")
    private List<Ticket> tickets;

    @PrePersist
    public void prePersist() {
        if (code == null) {
            long timestampPart = System.currentTimeMillis() % 1_000_000_000L;
            code = String.format("ORD%09d", timestampPart);
        }
    }
}
