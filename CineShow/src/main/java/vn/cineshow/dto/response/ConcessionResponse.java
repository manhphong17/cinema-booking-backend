package vn.cineshow.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import vn.cineshow.enums.ConcessionStatus;
import vn.cineshow.enums.ConcessionType;
import vn.cineshow.enums.StockStatus;

public record ConcessionResponse(
        Long concessionId,
        @NotBlank String name,
        @NotNull @Positive Double price,
        String description,
        @NotNull ConcessionType concessionType,
        @PositiveOrZero int unitInStock,
        @NotNull StockStatus stockStatus,
        @NotNull ConcessionStatus concessionStatus,
        String urlImage
) {
}
