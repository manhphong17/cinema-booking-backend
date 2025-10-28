package vn.cineshow.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.PaymentStatus;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment extends AbstractEntity implements Serializable {

    @ManyToOne
    Order order;

    @ManyToOne
    PaymentMethod method;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    Double amount;

    @Column(columnDefinition = "VARCHAR(100)")
    String transactionNo;

    @Column(columnDefinition = "VARCHAR(100)")
    String txnRef;

    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;

//    LocalDateTime paymentDate;

}
