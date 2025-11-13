package vn.cineshow.dto.request.room;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomRequest {
    @NotBlank
    String name;

    @NotNull
    Long roomTypeId;

    @NotNull @Min(1)@Max(12)
    Integer rows;

    @NotNull @Min(1)@Max(12)
    Integer columns;

    @NotBlank // "ACTIVE" | "INACTIVE"
    String status;

    String description;
    String screenType;
}
