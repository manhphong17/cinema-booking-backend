package vn.cineshow.dto.response.concession;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConcessionSimpleResponse {
    Long concessionId;
    String name;
    Double price;
}