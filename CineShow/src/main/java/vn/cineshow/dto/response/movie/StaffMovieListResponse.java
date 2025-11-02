package vn.cineshow.dto.response.movie;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffMovieListResponse implements Serializable {

    Long id;
    String name;
    String posterUrl;
    Integer duration;
    int ageRating;
}

