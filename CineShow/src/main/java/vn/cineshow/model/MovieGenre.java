package vn.cineshow.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Set;

@Entity()
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieGenre extends AbstractEntity implements Serializable {
    String name;
    @ManyToMany(mappedBy = "movieGenres")
    private Set<Movie> movies;
}
