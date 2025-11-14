package vn.cineshow.dto.response.theater;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class TheaterResponse {
    Long id;

    String name;
    String address;
    String hotline;
    String contactEmail;
    String googleMapUrl;

    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime openTime;
    
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime closeTime;
    
    Boolean overnight;


    String information;

    String createdBy;
    String updatedBy;
}
