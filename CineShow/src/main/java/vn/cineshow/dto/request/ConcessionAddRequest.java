package vn.cineshow.dto.request;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import vn.cineshow.enums.ConcessionType;

public record ConcessionAddRequest(

        @NotBlank(message = "Tên sản phẩm không được để trống")
        @Size(max = 100, message = "Tên sản phẩm tối đa 100 ký tự")
        String name,

        @NotNull(message = "Giá bán không được để trống")
        @Positive(message = "Giá bán phải lớn hơn 0")
        Double price,

        @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
        String description,

        @NotNull(message = "Loại sản phẩm không được để trống")
        ConcessionType concessionType,

        @NotNull(message = "Số lượng tồn kho không được để trống")
        @PositiveOrZero(message = "Số lượng tồn kho không thể là số âm")
        int unitInStock,

        @NotNull(message = "File ảnh không được để trống")
        @NotNull MultipartFile file

) {
}
