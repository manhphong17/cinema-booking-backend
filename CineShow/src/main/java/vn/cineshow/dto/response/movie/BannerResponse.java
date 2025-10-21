package vn.cineshow.dto.response.movie;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BannerResponse {
    String bannerUrl;
    Long movieId;
}
