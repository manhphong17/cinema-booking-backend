package vn.cineshow.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.cineshow.enums.Gender;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String address;
    private int loyalPoint;
    private Gender gender;
    private LocalDate dateOfBirth;
}
