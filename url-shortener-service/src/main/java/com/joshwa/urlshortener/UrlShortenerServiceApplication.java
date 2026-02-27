package com.joshwa.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UrlShortenerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerServiceApplication.class, args);
    }

}
