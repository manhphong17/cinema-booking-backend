package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.SeatShowTimeStatus;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tickets")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Ticket extends AbstractEntity implements Serializable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    Seat seat;

    Double price;

    @Enumerated(EnumType.STRING)
    SeatShowTimeStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    ShowTime showTime;

    @Column(nullable = false, unique = true)
    String code;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @PrePersist
    public void prePersist() {
        if (code == null && seat != null && showTime != null) {
            // Combine showTimeId, seatId, and the last 6 digits of current timestamp
            // This ensures uniqueness even if multiple tickets are created at the same time
            String raw = String.format("%d%d%d",
                    showTime.getId(),
                    seat.getId(),
                    System.currentTimeMillis() % 1_000_000);

            // Normalize to exactly 10 digits:
            // - If longer than 10 → keep only the last 10 digits
            // - If shorter than 10 → pad with zeros at the beginning
            if (raw.length() > 10) {
                code = raw.substring(raw.length() - 10);
            } else {
                code = String.format("%010d", Long.parseLong(raw));
            }
        }
    }

}
