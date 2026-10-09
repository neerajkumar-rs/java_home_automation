package com.example.homeautomation.device;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeviceDataInitializer {

    @Bean
    CommandLineRunner seedDevices(DeviceRepository repository) {
        return args -> {
            // Disabled for now to get application running
            System.out.println("DeviceDataInitializer: Skipping device seeding");
        };
    }
}
