package com.example.homeautomation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HomeAutomationApplication {

    public static void main(String[] args) throws IOException {
        // The SQLite JDBC driver cannot create the parent directory of the
        // database file itself, so create ./data before the datasource connects.
        Files.createDirectories(Path.of("./data"));
        SpringApplication.run(HomeAutomationApplication.class, args);
    }
}
