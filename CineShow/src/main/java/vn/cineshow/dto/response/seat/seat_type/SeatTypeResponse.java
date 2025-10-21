package vn.cineshow.dto.response.seat.seat_type;


import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatTypeResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean active;
}
