package com.example.restaurantreservation.service;

import com.example.restaurantreservation.entity.User;
import com.example.restaurantreservation.exception.UserNotFoundException;
import com.example.restaurantreservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new user with encoded password.
     */
    @Transactional
    public User createUser(String username, String password, Set<String> roles) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists: " + username);
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = User.builder()
                .username(username)
                .password(encodedPassword)
                .roles(roles)
                .build();

        return userRepository.save(user);
    }

    /**
     * Updates user password with encoding.
     */
    @Transactional
    public User updatePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        return userRepository.save(user);
    }

    /**
     * Updates user role.
     */
    @Transactional
    public User updateRole(String username, String newRole) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        user.getRoles().add(newRole);
        return userRepository.save(user);
    }

    /**
     * Gets the current authenticated user.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        String username = extractUsername(principal);

        log.debug("Extracted username: {}", username);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    private String extractUsername(Object principal) {
        // Case 1: CustomUserDetails or any UserDetails implementation
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        // Case 2: String (direct username)
        if (principal instanceof String principalName) {
            return principalName;
        }

        // Case 3: JWT Authentication Token
        if (principal instanceof JwtAuthenticationToken jwtToken) {
            Jwt jwt = jwtToken.getToken();
            return extractUsernameFromJwt(jwt);
        }

        // Case 4: Direct JWT
        if (principal instanceof Jwt jwt) {
            return extractUsernameFromJwt(jwt);
        }

        throw new IllegalStateException("Unsupported principal type: " +
                (principal != null ? principal.getClass().getName() : "null"));
    }

    private String extractUsernameFromJwt(Jwt jwt) {
        log.debug("Extracting username from JWT. Claims: {}", jwt.getClaims().keySet());

        // Priority 1: client_id (for machine-to-machine authentication)
        String clientId = jwt.getClaim("client_id");
        if (clientId != null && !clientId.isEmpty()) {
            log.debug("Using client_id: {}", clientId);
            return clientId;
        }

        // Priority 2: preferred_username (for user authentication)
        String preferredUsername = jwt.getClaim("preferred_username");
        if (preferredUsername != null && !preferredUsername.isEmpty()) {
            log.debug("Using preferred_username: {}", preferredUsername);
            return preferredUsername;
        }

        // Priority 3: email
        String email = jwt.getClaim("email");
        if (email != null && !email.isEmpty()) {
            log.debug("Using email: {}", email);
            return email;
        }

        // Priority 4: subject (fallback)
        String subject = jwt.getSubject();
        if (subject != null && !subject.isEmpty()) {
            log.debug("Using subject: {}", subject);
            return subject;
        }

        throw new IllegalStateException("No suitable username claim found in JWT. Available: " + jwt.getClaims().keySet());
    }

    /**
     * Gets current username without loading the full user object.
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        return authentication.getName();
    }
}
