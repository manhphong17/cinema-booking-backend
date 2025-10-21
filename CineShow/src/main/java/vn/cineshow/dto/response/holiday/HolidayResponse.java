package vn.cineshow.dto.response.holiday;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HolidayResponse {
private Long id;
    private String description;
    private String date; // format chung: nếu recurring → "MM-DD", nếu yearly → "YYYY-MM-DD"
    @JsonProperty("isRecurring")
    private boolean isRecurring;
}