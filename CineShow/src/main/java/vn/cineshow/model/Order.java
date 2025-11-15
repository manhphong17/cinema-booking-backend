package vn.cineshow.model;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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

    @Column(columnDefinition = "decimal(10,2) DEFAULT 0.00")
    Double discount;

    @Enumerated(EnumType.STRING)
    OrderStatus orderStatus; //PENDING, COMPLETED, CANCELED

    @Column(nullable = false, unique = true)
    String code;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, mappedBy = "order")
    private Payment payment;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE} , fetch = FetchType.LAZY, mappedBy = "order")
    private List<OrderConcession> orderConcession;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, mappedBy = "order")
    private List<Ticket> tickets;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean DEFAULT false")
    Boolean isCheckIn = false;

    @PrePersist
    public void prePersist() {
        if (code == null) {
            long timestampPart = System.currentTimeMillis() % 1_000_000_000L;
            code = String.format("PHT%09d", timestampPart);
        }
        if (isCheckIn == null) {
            isCheckIn = false;
        }
    }

}
