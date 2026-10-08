package com.example.homeautomation.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.stereotype.Component;

@Component
public class DBConnection {

    private static final String URL = "jdbc:sqlite:home_automation.db";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
