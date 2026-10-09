package com.example.homeautomation.user;

import com.example.homeautomation.user.dto.AuthResponse;
import com.example.homeautomation.user.dto.LoginRequest;
import com.example.homeautomation.user.dto.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;

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
        // The users table (shared with security.SecureUser) declares name,
        // created_at and updated_at as NOT NULL, so they must be populated.
        user.setName(request.getFirstName() + " " + request.getLastName());
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // Hash password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Registration ALWAYS creates a plain USER. Any role/admin flag sent
        // by the client is deliberately ignored. Stored without ROLE_ prefix;
        // the JWT filter adds ROLE_ exactly once.
        user.setRole("USER");

        // Save user
        User savedUser = userRepository.save(user);
        
        // Generate token
        String token = jwtTokenService.generateToken(savedUser);
        
        return new AuthResponse(savedUser, token);
    }
    
    // Deliberately NOT @Transactional: the failed-attempt counter and lock flag
    // must COMMIT even when login fails. If this method were transactional, the
    // RuntimeException for a bad password would roll the whole transaction back
    // and the counter would never persist (verified: counter stayed 0 in the DB).
    // Each userRepository.save() below commits in its own transaction instead.
    public AuthResponse login(LoginRequest request) {
        // One generic message for both unknown user and wrong password,
        // so the response cannot be used to enumerate accounts.
        final String genericError = "Invalid username or password";

        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        if (userOptional.isEmpty()) {
            throw new RuntimeException(genericError);
        }

        User user = userOptional.get();

        // Check if user is active
        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        // Null-safe: rows created before this column existed have NULL.
        int failedAttempts = user.getFailedAttempts() == null ? 0 : user.getFailedAttempts();

        // Temporary lock: 15 minutes, measured from the last failed attempt.
        if (user.isLocked()) {
            LocalDateTime lockedAt = user.getLastFailedLogin();
            if (lockedAt != null && lockedAt.plusMinutes(LOCK_DURATION_MINUTES).isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Account is temporarily locked. Please try again later.");
            }
            // Lock expired -> auto-unlock and start over.
            user.setLocked(false);
            failedAttempts = 0;
            user.setFailedAttempts(0);
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            failedAttempts++;
            user.setFailedAttempts(failedAttempts);
            user.setLastFailedLogin(LocalDateTime.now());
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                user.setLocked(true);
            }
            log.debug("Failed login attempt {}/{} for user '{}'", failedAttempts, MAX_FAILED_ATTEMPTS, request.getUsername());
            userRepository.save(user);
            throw new RuntimeException(genericError);
        }

        // Success: reset counters, update last login
        user.setFailedAttempts(0);
        user.setLocked(false);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate token
        String token = jwtTokenService.generateToken(user);

        return new AuthResponse(user, token);
    }
    
    @Transactional(readOnly = true)
    public User getCurrentUser(String token) {
        // Token subject is the user's email.
        String email = jwtTokenService.extractUsername(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return jwtTokenService.validateToken(token);
    }
}