package vn.cineshow.dto.request;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import vn.cineshow.enums.ConcessionType;

public record ConcessionUpdateRequest(

        @NotBlank(message = "Tên sản phẩm không được để trống")
        @Size(max = 100)
        String name,

        @NotNull(message = "Giá bán không được để trống")
        @Positive
        Double price,

        @Size(max = 500)
        String description,

        @NotNull
        ConcessionType concessionType,

        @NotNull
        @PositiveOrZero(message = "Số lượng tồn kho không thể là số âm")
        int unitInStock,

        MultipartFile file
) {
}
