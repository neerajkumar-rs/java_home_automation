package com.example.homeautomation.user;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds exactly one admin user, only when the users table is completely empty.
 * Credentials come from the APP_ADMIN_EMAIL / APP_ADMIN_PASSWORD environment
 * variables; nothing is hard-coded and nothing is created when they are absent.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("DataSeeder: users table is not empty, skipping admin seeding");
            return;
        }

        String email = System.getenv("APP_ADMIN_EMAIL");
        String password = System.getenv("APP_ADMIN_PASSWORD");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            log.warn("DataSeeder: APP_ADMIN_EMAIL and/or APP_ADMIN_PASSWORD not set; no admin user created");
            return;
        }

        // Login looks users up by username, so derive one from the email local part.
        String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;

        // NOTE: the users table is shared with the security.SecureUser entity,
        // whose DDL makes first_name/last_name/created_at/updated_at NOT NULL.
        // All four must be set here or the insert violates those constraints.
        LocalDateTime now = LocalDateTime.now();

        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setName(username);
        admin.setFirstName(username);
        admin.setLastName(username);
        admin.setPassword(passwordEncoder.encode(password));
        // Role convention: stored without prefix; filter adds ROLE_ once.
        admin.setRole("ADMIN");
        admin.setActive(true);
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);
        userRepository.save(admin);

        log.info("DataSeeder: admin user created (email={})", email);
    }
}
