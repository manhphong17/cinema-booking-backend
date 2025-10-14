package vn.cineshow.model.ids;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrderConcessionId {
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "concession_id")
    private Long concessionId;

}
