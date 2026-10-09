package com.example.homeautomation.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * JWT User Details Service
 * 
 * Responsible for:
 * 1. Loading user by username (for authentication)
 * 2. Loading user by email (for data isolation context)
 * 3. Creating UserDetails for Spring Security
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {
    
    private final SecureUserRepository userRepository;
    
    public JwtUserDetailsService(SecureUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Load user by username (for JWT authentication)
     * This is called during login to validate credentials
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try to find user by username (for backward compatibility)
        SecureUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        // Check if user is active
        if (!user.isActive()) {
            throw new UsernameNotFoundException("User account is inactive");
        }
        
        // Check if user is locked
        if (user.isLocked()) {
            throw new UsernameNotFoundException("User account is locked");
        }
        
        return createUserDetails(user);
    }
    
    /**
     * Load user by email (for data isolation)
     * This is called from JWT token validation to establish user context
     */
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        SecureUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        // Check if user is active
        if (!user.isActive()) {
            throw new UsernameNotFoundException("User account is inactive");
        }
        
        return createUserDetails(user);
    }
    
    /**
     * Create Spring Security UserDetails from SecureUser
     */
    private UserDetails createUserDetails(SecureUser user) {
        // Convert roles to Spring Security format
        java.util.Collection<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        for (String role : user.getRoles()) {
            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(role));
        }
        
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // Use EMAIL as principal (for data isolation)
                user.getPassword(),
                user.isActive(),
                true, // account non-expired
                true, // credentials non-expired
                !user.isLocked(), // account non-locked
                authorities
        );
    }
    
    /**
     * Validate JWT token and return user email
     * This is used to extract the authenticated user context
     */
    public String extractUserEmailFromToken(String token) {
        // In real implementation, decode JWT token and extract email claim
        // For now, placeholder - actual JWT parsing would go here
        
        // This would typically parse the JWT token and extract the "sub" claim
        // return jwtUtil.extractEmail(token);
        
        // For this demo, we'll just return a placeholder
        // In production, use JwtTokenUtil or similar
        return "admin@homeautomation.com"; // Placeholder
    }
    
    /**
     * Record successful login
     */
    public void recordSuccessfulLogin(String username) {
        SecureUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        user.recordSuccessfulLogin();
        userRepository.save(user);
    }
    
    /**
     * Record failed login
     */
    public void recordFailedLogin(String username) {
        SecureUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        user.recordFailedLogin();
        userRepository.save(user);
    }
}