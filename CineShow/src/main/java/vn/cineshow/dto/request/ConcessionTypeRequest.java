package vn.cineshow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcessionTypeRequest {
    @NotBlank(message = "Tên loại sản phẩm không được để trống.")
    private String name;
}
