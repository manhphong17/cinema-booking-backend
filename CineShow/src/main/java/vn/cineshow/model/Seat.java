package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.SeatStatus;

import java.io.Serializable;

@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Seat extends AbstractEntity implements Serializable {
    String seatNumber;
    @Column(name = "seat_row")
    String row;

    @Column(name = "seat_column")
    String column;

    @Enumerated(EnumType.STRING)
    SeatStatus status;

    Double price;

    @ManyToOne
    Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_type_id", nullable = false)
    private SeatType seatType;

}
