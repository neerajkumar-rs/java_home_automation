package com.example.homeautomation.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.homeautomation.jdbc.DBConnection;

@Repository
public class UserDAO {

    private final DBConnection dbConnection;

    public UserDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public void createTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    role TEXT NOT NULL,
                    password TEXT NOT NULL,
                    active INTEGER NOT NULL DEFAULT 1
                )
                """;

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "ALTER TABLE users ADD COLUMN password TEXT NOT NULL DEFAULT 'password'")) {
            try {
                statement.executeUpdate();
            } catch (SQLException ignored) {
            }
        }
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name, email, role, active FROM users ORDER BY id";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(new User(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("role"),
                        resultSet.getInt("active") == 1
                ));
            }
        }

        return users;
    }

    public User create(User user) throws SQLException {
        String sql = """
                INSERT INTO users
                (name, email, role, password, active)
                VALUES (?, ?, ?, ?, 1)
                """;

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getRole());
            statement.setString(4, user.getPassword());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(
                            keys.getLong(1),
                            user.getName(),
                            user.getEmail(),
                            user.getRole(),
                            true,
                            user.getPassword()
                    );
                }
            }
        }

        throw new SQLException("Unable to create user");
    }

    public User findByEmail(String email) throws SQLException {
        String sql = """
                SELECT id, name, email, role, active, password
                FROM users
                WHERE email = ?
                """;

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(
                            resultSet.getLong("id"),
                            resultSet.getString("name"),
                            resultSet.getString("email"),
                            resultSet.getString("role"),
                            resultSet.getInt("active") == 1,
                            resultSet.getString("password")
                    );
                }
            }
        }

        return null;
    }

    public void updateActive(Long id, boolean active) throws SQLException {
        String sql = "UPDATE users SET active = ? WHERE id = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, active ? 1 : 0);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }
}