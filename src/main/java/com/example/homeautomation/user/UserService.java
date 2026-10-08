package com.example.homeautomation.user;

import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void initialize() throws SQLException {
        userDAO.createTable();
    }

    public List<User> findAll() throws SQLException {
        return userDAO.findAll();
    }

    public User create(User user) throws SQLException {
        if (user.getName() == null || user.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (user.getRole() == null || user.getRole().isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        return userDAO.create(user);
    }

    public User login(String email, String password) throws SQLException {
        User user = userDAO.findByEmail(email);

        if (user == null || !user.isActive()) {
            return null;
        }

        if (!user.getPassword().equals(password)) {
            return null;
        }

        return user;
    }

    public void setActive(Long id, boolean active) throws SQLException {
        userDAO.updateActive(id, active);
    }
}