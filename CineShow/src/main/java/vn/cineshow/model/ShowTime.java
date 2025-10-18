package vn.cineshow.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Table(name = "showtimes")
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowTime extends AbstractEntity implements Serializable {

    @Column(nullable = false, columnDefinition = "DATETIME(6)")
    LocalDateTime startTime;

    @Column(nullable = false, columnDefinition = "DATETIME(6)")
    LocalDateTime  endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    Room room;

    @OneToMany(mappedBy = "showTime", fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    Movie movie;

    @OneToMany(mappedBy = "showTime", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<TicketPrice> ticketPrices;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtitle_id", nullable = false)
    private SubTitle subtitle;
}
