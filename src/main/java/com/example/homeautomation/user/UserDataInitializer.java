package com.example.homeautomation.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserDataInitializer {

    @Bean
    CommandLineRunner initializeUsers(UserService userService) {
        return args -> {
            userService.initialize();

            if (userService.findAll().isEmpty()) {
                userService.create(new User("Raj", "raj@home.local", "HOMEOWNER", "raj123"));
                userService.create(new User("Admin", "admin@home.local", "ADMIN", "admin123"));
            }
        };
    }
}
