package com.example.homeautomation.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

@Configuration
public class UserDataInitializer {

    @Bean
    CommandLineRunner initializeUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if users already exist
            if (userRepository.count() == 0) {
                System.out.println("UserDataInitializer: Creating default users...");
                
                // Create admin user
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@homeautomation.com");
                admin.setName("Admin User");  // Set name field
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ROLE_ADMIN");
                admin.setActive(true);
                // Set created_at timestamp
                admin.setCreatedAt(LocalDateTime.now());
                userRepository.save(admin);
                
                // Create regular user
                User user = new User();
                user.setUsername("user");
                user.setEmail("user@homeautomation.com");
                user.setName("Regular User");  // Set name field
                user.setFirstName("Regular");
                user.setLastName("User");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setRole("ROLE_USER");
                user.setActive(true);
                // Set created_at timestamp
                user.setCreatedAt(LocalDateTime.now());
                userRepository.save(user);
                
                System.out.println("UserDataInitializer: Created admin/admin123 and user/user123");
            } else {
                System.out.println("UserDataInitializer: Users already exist, skipping seeding");
            }
        };
    }
}
