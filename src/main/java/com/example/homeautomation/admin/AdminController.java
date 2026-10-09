package com.example.homeautomation.admin;

import com.example.homeautomation.security.SecureDevice;
import com.example.homeautomation.security.SecureDeviceRepository;
import com.example.homeautomation.user.User;
import com.example.homeautomation.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    private final UserRepository userRepository;
    private final SecureDeviceRepository secureDeviceRepository;

    public AdminController(UserRepository userRepository, SecureDeviceRepository secureDeviceRepository) {
        this.userRepository = userRepository;
        this.secureDeviceRepository = secureDeviceRepository;
    }

    /**
     * The ONLY place that can see every device regardless of owner.
     * Returns a DTO, not the entity, and runs readOnly so the LAZY owner
     * association can be resolved without LazyInitializationException.
     */
    @GetMapping("/devices")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAllDevices() {
        List<Map<String, Object>> devices = secureDeviceRepository.findAll().stream()
                .map(this::toDeviceView)
                .collect(Collectors.toList());
        return ResponseEntity.ok(devices);
    }

    private Map<String, Object> toDeviceView(SecureDevice d) {
        Map<String, Object> view = new java.util.HashMap<>();
        view.put("id", d.getId());
        view.put("deviceUid", d.getDeviceUid());
        view.put("name", d.getName());
        view.put("type", d.getType());
        view.put("room", d.getRoom());
        view.put("state", d.getState());
        view.put("value", d.getValue());
        view.put("active", d.isActive());
        view.put("ownerEmail", d.getOwnerEmail());
        return view;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            // Check if username already exists
            if (userRepository.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username already exists"));
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email already exists"));
            }
            
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            return userRepository.findById(id)
                    .map(existingUser -> {
                        // Update user fields
                        existingUser.setUsername(user.getUsername());
                        existingUser.setEmail(user.getEmail());
                        existingUser.setFirstName(user.getFirstName());
                        existingUser.setLastName(user.getLastName());
                        // Role stored without prefix; setAdmin assigns ADMIN or USER.
                        existingUser.setAdmin(user.isAdmin());
                        existingUser.setActive(user.isActive());

                        User updatedUser = userRepository.save(existingUser);
                        return ResponseEntity.ok(updatedUser);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            // Prevent deleting the default admin user
            return userRepository.findById(id)
                    .map(user -> {
                        if ("admin".equals(user.getUsername())) {
                            return ResponseEntity.badRequest()
                                    .body(Map.of("error", "Cannot delete default admin user"));
                        }
                        
                        userRepository.delete(user);
                        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/users/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setActive(true);
                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/users/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setActive(false);
                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/system/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSystemStats() {
        try {
            long userCount = userRepository.count();
            long activeUserCount = userRepository.findAll().stream()
                    .filter(User::isActive)
                    .count();
            long adminCount = userRepository.findAll().stream()
                    .filter(User::isAdmin)
                    .count();
            
            return ResponseEntity.ok(Map.of(
                "users", Map.of(
                    "total", userCount,
                    "active", activeUserCount,
                    "admins", adminCount
                ),
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}