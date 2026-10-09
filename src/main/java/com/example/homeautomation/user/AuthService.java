package com.example.homeautomation.user;

import com.example.homeautomation.user.dto.AuthResponse;
import com.example.homeautomation.user.dto.LoginRequest;
import com.example.homeautomation.user.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        
        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        // Hash password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Set admin role if requested
        if (request.isAdmin()) {
            user.setAdmin(true);
        }
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Generate token
        String token = jwtTokenService.generateToken(savedUser);
        
        return new AuthResponse(savedUser, token);
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Find user by username
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
        
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid username or password");
        }
        
        User user = userOptional.get();
        
        // Check if user is active
        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // Generate token
        String token = jwtTokenService.generateToken(user);
        
        return new AuthResponse(user, token);
    }
    
    @Transactional(readOnly = true)
    public User getCurrentUser(String token) {
        String username = jwtTokenService.extractUsername(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return jwtTokenService.validateToken(token);
    }
}