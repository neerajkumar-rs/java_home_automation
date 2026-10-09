package com.example.homeautomation.user;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "role", nullable = false)
    private String role;
    
    @Column(name = "active", nullable = false)
    private boolean active = true;
    
    @Column(name = "locked", nullable = false)
    private boolean locked = false;
    
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    
    @JsonIgnore
    @Column
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
    
    public void setName(String name) {
        this.name = name;
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
    
    // Add missing methods that the code expects
    public String getUsername() {
        return username != null ? username : email; // Prefer username, fallback to email
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
    }
    
    public void setAdmin(boolean admin) {
        this.role = admin ? "ROLE_ADMIN" : "user";
    }
    
    public java.util.Set<String> getRoles() {
        return java.util.Set.of(role != null ? role : "user");
    }
    
    public void addRole(String role) {
        // Simple implementation - just set the role
        this.role = role;
    }
    
    public void removeRole(String role) {
        // Simple implementation - clear if matches
        if (role.equals(this.role)) {
            this.role = "user";
        }
    }
    
    public java.time.LocalDateTime getLastLogin() {
        return null; // Old User doesn't track last login
    }
    
    public void setLastLogin(java.time.LocalDateTime lastLogin) {
        // Do nothing - old User doesn't track last login
    }
    
    public boolean isLocked() {
        return locked;
    }
    
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    
    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    

    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public User() {
        // Default constructor
        this.updatedAt = java.time.LocalDateTime.now();
        this.createdAt = java.time.LocalDateTime.now();
    }
}