package com.example.homeautomation.user;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {

    private Long id;
    private String name;
    private String email;
    private String role;
    private boolean active;

    @JsonIgnore
    private String password;

    public User(Long id, String name, String email, String role, boolean active) {
        this(id, name, email, role, active, null);
    }

    public User(Long id, String name, String email, String role,
                boolean active, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
        this.password = password;
    }

    public User(String name, String email, String role) {
        this(null, name, email, role, true, null);
    }

    public User(String name, String email, String role, String password) {
        this(null, name, email, role, true, password);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public String getPassword() {
        return password;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}