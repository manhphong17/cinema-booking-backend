package vn.cineshow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AgeRating {
    P(0, "Phù hợp với mọi lứa tuổi"),
    K(1, "Trẻ em"),
    T13(2, "Từ 13 tuổi trở lên"),
    T16(3, "Từ 16 tuổi trở lên"),
    T18(4, "Từ 18 tuổi trở lên");

    private final int code;
    private final String description;
}
