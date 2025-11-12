package vn.cineshow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "payment_methods")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentMethod extends AbstractEntity implements Serializable {

    @Column(length = 50, nullable = false, unique = true)
    String methodName;

    boolean isActive;

    @OneToMany(mappedBy = "method")
    private List<Payment> payments;

    @Column(name = "payment_code", length = 50, unique = true)
    private String paymentCode;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "bank_name")
    private String  bankName;


}
