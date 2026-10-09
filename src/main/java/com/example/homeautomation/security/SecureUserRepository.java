package com.example.homeautomation.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Secure User Repository
 * 
 * Key Features:
 * - Email as primary key (as requested)
 * - User lookup by username for authentication
 * - Email-based queries for data isolation
 */
@Repository
public interface SecureUserRepository extends JpaRepository<SecureUser, String> {
    
    // Primary key is email, so this method is automatically available
    Optional<SecureUser> findByEmail(String email);
    
    // Find by username (for authentication)
    Optional<SecureUser> findByUsername(String username);
    
    // Find active user by username
    @Query("SELECT u FROM SecureUser u WHERE u.username = :username AND u.active = true")
    Optional<SecureUser> findActiveByUsername(@Param("username") String username);
    
    // Find user with specific role
    @Query("SELECT u FROM SecureUser u JOIN u.roles r WHERE u.email = :email AND r = :role")
    Optional<SecureUser> findByEmailAndRole(@Param("email") String email, @Param("role") String role);
    
    // Check if username exists (for registration)
    boolean existsByUsername(String username);
    
    // Check if email exists (for registration)
    boolean existsByEmail(String email);
    
    // Count active users
    long countByActiveTrue();
    
    // Count users by role
    @Query("SELECT COUNT(DISTINCT u) FROM SecureUser u JOIN u.roles r WHERE r = :role")
    long countByRole(@Param("role") String role);
    
    // Find users who locked their accounts
    @Query("SELECT u FROM SecureUser u WHERE u.locked = true")
    java.util.List<SecureUser> findLockedUsers();
    
    // Find users who have not logged in recently
    @Query("SELECT u FROM SecureUser u WHERE u.lastLogin < :cutoffDate")
    java.util.List<SecureUser> findInactiveUsers(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}