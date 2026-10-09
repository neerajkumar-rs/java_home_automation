package com.example.homeautomation.config;

import com.example.homeautomation.user.JwtTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // Parse once; any JwtException below means the token is rejected.
            String email = jwtTokenService.extractUsername(token); // subject = email

            List<String> roles = jwtTokenService.extractClaim(token, claims -> {
                Object raw = claims.get("roles");
                if (raw instanceof List<?> list) {
                    return list.stream().map(String::valueOf).collect(Collectors.toList());
                }
                if (raw instanceof String s && !s.isBlank()) {
                    return List.of(s);
                }
                return Collections.<String>emptyList();
            });

            // Token roles carry no prefix; add ROLE_ exactly once.
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // The email goes into the SecurityContext as the principal name.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ExpiredJwtException e) {
            log.debug("Rejected token for {} {}: expired at {}", request.getMethod(), request.getRequestURI(), e.getClaims().getExpiration());
            SecurityContextHolder.clearContext();
        } catch (SignatureException e) {
            log.debug("Rejected token for {} {}: invalid signature", request.getMethod(), request.getRequestURI());
            SecurityContextHolder.clearContext();
        } catch (MalformedJwtException e) {
            log.debug("Rejected token for {} {}: malformed token ({})", request.getMethod(), request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            log.debug("Rejected token for {} {}: {}", request.getMethod(), request.getRequestURI(), e.toString());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
