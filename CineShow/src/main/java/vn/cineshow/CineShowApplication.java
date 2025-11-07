package vn.cineshow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CineShowApplication {
    public static void main(String[] args) {
        SpringApplication.run(CineShowApplication.class, args);
    }

}
