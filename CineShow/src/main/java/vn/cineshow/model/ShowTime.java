package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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
    LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    Room room;

    @OneToMany(mappedBy = "showTime", fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtitle_id", nullable = false)
    private SubTitle subtitle;

    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted = false;
}
