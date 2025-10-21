package vn.cineshow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Getter
@Table(name = "seat_types")
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatType extends AbstractEntity implements Serializable {

    @Column(nullable = false, length = 50)
    String name;
    String description;


//    @Column(name = "code", nullable = false, unique = true, length = 50)
//    String code;

}
