package vn.cineshow;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
public class CineShowApplication {
    
    @PostConstruct
    public void init() {
        // Set default timezone to Vietnam (Asia/Ho_Chi_Minh)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }
    
    public static void main(String[] args) {
        SpringApplication.run(CineShowApplication.class, args);
    }

}
