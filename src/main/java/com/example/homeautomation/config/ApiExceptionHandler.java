package com.example.homeautomation.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Single error handler for the whole API. Every error returns the same JSON
 * shape: timestamp, status, error, message, path. Never leaks stack traces.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message, HttpServletRequest req) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        error.put("path", req.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }

    /** Bean Validation failures (@Valid on @RequestBody) -> 400 with field details. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException e, HttpServletRequest req) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (message.isBlank()) message = "Validation failed";
        return body(HttpStatus.BAD_REQUEST, message, req);
    }

    /** Malformed JSON or invalid enum value in the request body -> 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> unreadable(HttpMessageNotReadableException e, HttpServletRequest req) {
        return body(HttpStatus.BAD_REQUEST, "Malformed request body or invalid value", req);
    }

    /** Carries its own status (404 not-found-or-foreign, 409 duplicate, ...). */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> status(ResponseStatusException e, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        String reason = e.getReason() != null ? e.getReason() : status.getReasonPhrase();
        return body(status, reason, req);
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class,
            ExpiredJwtException.class, SignatureException.class, MalformedJwtException.class})
    public ResponseEntity<Map<String, Object>> unauthorized(Exception e, HttpServletRequest req) {
        return body(HttpStatus.UNAUTHORIZED, "Authentication failed", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> forbidden(AccessDeniedException e, HttpServletRequest req) {
        return body(HttpStatus.FORBIDDEN, "Access denied", req);
    }

    /** Safety net: unexpected errors -> 500 with a generic message, no stack trace. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> unexpected(Exception e, HttpServletRequest req) {
        log.error("Unhandled error on {} {}", req.getMethod(), req.getRequestURI(), e);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", req);
    }
}
