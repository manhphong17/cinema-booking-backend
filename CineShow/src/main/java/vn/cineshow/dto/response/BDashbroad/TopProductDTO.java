package vn.cineshow.dto.response.BDashbroad;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopProductDTO {
    private String name;
    private double revenue;
    private long totalSold;
    private String urlImage;
}
