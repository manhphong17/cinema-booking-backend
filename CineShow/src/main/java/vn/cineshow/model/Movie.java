package vn.cineshow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.MovieStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Movie extends AbstractEntity implements Serializable {

    @Column(nullable = false, length = 200)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(nullable = false)
    int duration;

    @Column(nullable = false)
    LocalDate releaseDate;

    private String posterUrl;

    private String trailerUrl;

    private int ageRating = 0;

    private String director;

    private String actor;

    @Enumerated(EnumType.STRING)
    private MovieStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @ManyToMany()
    private Set<MovieGenre> movieGenres;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "movie")
    private List<ShowTime> showTimes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    @JsonIgnore
    private Country country;

    Boolean isDeleted = false;

}
