package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.ConcessionStatus;
import vn.cineshow.enums.ConcessionType;
import vn.cineshow.enums.StockStatus;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "Concession")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Concession extends AbstractEntity implements Serializable {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "decimal(10,2)", nullable = false)
    private Double price;

    private String description;

    @OneToMany(mappedBy = "concession", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<OrderConcession> orderConcessions;


    @Enumerated(EnumType.STRING)
    private ConcessionType concessionType;

    private int unitInStock;

    @Enumerated(EnumType.STRING)
    private StockStatus stockStatus;

    @Enumerated(EnumType.STRING)
    private ConcessionStatus concessionStatus;

    private String urlImage;
}
