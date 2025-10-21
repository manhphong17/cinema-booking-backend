package vn.cineshow.dto.response.concession;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import vn.cineshow.enums.ConcessionStatus;
import vn.cineshow.enums.StockStatus;

@Builder
public record ConcessionResponse(
        Long concessionId,
        @NotBlank String name,
        @NotNull @Positive Double price,
        String description,
        @NotNull ConcessionTypeResponse concessionType,
        @PositiveOrZero int unitInStock,
        @NotNull StockStatus stockStatus,
        @NotNull ConcessionStatus concessionStatus,
        String urlImage
) {
}
