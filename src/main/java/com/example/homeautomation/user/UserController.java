package com.example.homeautomation.user;

import java.sql.SQLException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getUsers() {
        try {
            return userService.findAll();
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to load users", e);
        }
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        try {
            return userService.create(user);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to create user", e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping("/{id}/activate")
    public void activate(@PathVariable Long id) {
        updateStatus(id, true);
    }

    @PostMapping("/{id}/deactivate")
    public void deactivate(@PathVariable Long id) {
        updateStatus(id, false);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "User deletion will be added with the admin dashboard");
    }

    private void updateStatus(Long id, boolean active) {
        try {
            userService.setActive(id, active);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to update user", e);
        }
    }
}