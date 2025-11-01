package vn.cineshow.dto.redis;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConcessionOrderRequest implements Serializable {

    Long comboId;
    Integer quantity;
    Double unitPrice;
}