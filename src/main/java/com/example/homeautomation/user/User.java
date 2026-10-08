package com.example.homeautomation.user;

public class User {

    private Long id;
    private String name;
    private String email;
    private String role;
    private boolean active;

    public User(Long id, String name, String email, String role, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    public User(String name, String email, String role) {
        this(null, name, email, role, true);
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

    public void setActive(boolean active) {
        this.active = active;
    }
}
