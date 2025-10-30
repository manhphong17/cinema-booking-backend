package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import vn.cineshow.model.ids.OrderConcessionId;

import java.io.Serializable;

@Entity
@Table(name = "order_concessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderConcession implements Serializable {

    @EmbeddedId
    private OrderConcessionId orderConcessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderId")
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("concessionId")
    @JoinColumn(name = "concession_id", nullable = false)
    private Concession concession;

    @Column(nullable = false)
    private int quantity;

    @Column(columnDefinition = "decimal(10,2)")
    private Double unitPrice = 0.00;

    @Column(columnDefinition = "DECIMAL(10,2)")
    Double priceSnapshot;
}
