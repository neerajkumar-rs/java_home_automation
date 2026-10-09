package com.example.homeautomation.security;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class SecureUser {
    
    @Id
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email must be less than 255 characters")
    @Column(name = "email", unique = true, nullable = false)
    private String email;  // PRIMARY KEY
    
    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username must be less than 50 characters")
    @Column(nullable = false, unique = true)
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(max = 255, message = "Password must be less than 255 characters")
    @Column(nullable = false)
    private String password;
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be less than 50 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be less than 50 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(name = "active", nullable = false)
    private boolean active = true;
    
    @Column(name = "locked", nullable = false)
    private boolean locked = false;
    
    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;
    
    @Column(name = "last_failed_login")
    private LocalDateTime lastFailedLogin;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_email"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();
    
    // One-to-many relationship: User owns devices
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SecureDevice> devices = new HashSet<>();
    
    // One-to-many relationship: User creates automations
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SecureAutomationRule> automationRules = new HashSet<>();
    
    // Constructor
    public SecureUser() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastPasswordChange = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
        this.lastPasswordChange = LocalDateTime.now();
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
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isLocked() {
        return locked;
    }
    
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    
    public Integer getFailedAttempts() {
        return failedAttempts;
    }
    
    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public LocalDateTime getLastPasswordChange() {
        return lastPasswordChange;
    }
    
    public void setLastPasswordChange(LocalDateTime lastPasswordChange) {
        this.lastPasswordChange = lastPasswordChange;
    }
    
    public LocalDateTime getLastFailedLogin() {
        return lastFailedLogin;
    }
    
    public void setLastFailedLogin(LocalDateTime lastFailedLogin) {
        this.lastFailedLogin = lastFailedLogin;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Set<String> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
    
    public void addRole(String role) {
        this.roles.add(role);
    }
    
    public void removeRole(String role) {
        this.roles.remove(role);
    }
    
    public Set<SecureDevice> getDevices() {
        return devices;
    }
    
    public void setDevices(Set<SecureDevice> devices) {
        this.devices = devices;
    }
    
    public void addDevice(SecureDevice device) {
        device.setOwner(this);
        this.devices.add(device);
    }
    
    public Set<SecureAutomationRule> getAutomationRules() {
        return automationRules;
    }
    
    public void setAutomationRules(Set<SecureAutomationRule> automationRules) {
        this.automationRules = automationRules;
    }
    
    // Business methods
    public void recordSuccessfulLogin() {
        this.lastLogin = LocalDateTime.now();
        this.failedAttempts = 0;
        this.locked = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void recordFailedLogin() {
        this.failedAttempts++;
        this.lastFailedLogin = LocalDateTime.now();
        
        if (this.failedAttempts >= 5) {
            this.locked = true;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean hasRole(String role) {
        return this.roles.contains(role);
    }
    
    public boolean isAdmin() {
        // Roles are stored without the ROLE_ prefix.
        return hasRole("ADMIN");
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "SecureUser{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", active=" + active +
                ", locked=" + locked +
                ", roles=" + roles +
                '}';
    }
}