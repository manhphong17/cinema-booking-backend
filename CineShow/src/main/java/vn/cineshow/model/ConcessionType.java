package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import vn.cineshow.enums.ConcessionTypeStatus;

import java.util.List;

@Entity
@Table(name = "concession_type")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcessionType extends AbstractEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConcessionTypeStatus status;

    @OneToMany(mappedBy = "concessionType", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<Concession> concessions;
}
