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
import vn.cineshow.enums.DayType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "ticket_prices",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"seat_type_id", "room_type_id", "day_type"})
        }
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketPrice extends AbstractEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_type_id", nullable = false)
    SeatType seatType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false)
    DayType dayType;

    @Column(columnDefinition = "DECIMAL(10,2)", nullable = false)
    Double price;
}
