package vn.cineshow.model;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.SeatStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "seats",
        uniqueConstraints = {
                // code là duy nhất trong phạm vi 1 phòng
                @UniqueConstraint(name = "uk_seat_room_code", columnNames = {"room_id", "code"})
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Seat extends AbstractEntity implements Serializable {
    String seatNumber;
    @Column(name = "seat_row")
    String row;

    @Column(name = "seat_column")
    String column;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    SeatStatus status;

    Double price;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    Room room;

    @Column(name = "row_label", length = 4)
    String rowLabel;

    @Column(name = "code", nullable = false, length = 16)
    String code;

    @Column
    Boolean blocked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_type_id", nullable = false)
    private SeatType seatType;

}
