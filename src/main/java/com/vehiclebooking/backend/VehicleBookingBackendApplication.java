package com.vehiclebooking.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class VehicleBookingBackendApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Africa/Johannesburg"));
        System.out.println("Application running in SAST: " + java.time.LocalDateTime.now());
    }

    public static void main(String[] args) {
        SpringApplication.run(VehicleBookingBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner generateHash() {
        return args -> {
            System.out.println("SYNC HASH: " + new BCryptPasswordEncoder().encode("password123"));
        };
    }
}
