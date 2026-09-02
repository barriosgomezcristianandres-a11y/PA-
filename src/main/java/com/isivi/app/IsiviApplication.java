package com.isivi.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IsiviApplication {

    public static void main(String[] args) {
        SpringApplication.run(IsiviApplication.class, args);
    }

}
