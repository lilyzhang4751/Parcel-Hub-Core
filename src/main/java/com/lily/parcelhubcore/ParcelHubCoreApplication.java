package com.lily.parcelhubcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ParcelHubCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParcelHubCoreApplication.class, args);
    }

}
