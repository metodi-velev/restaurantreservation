package com.example.restaurantreservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Optional;
import java.util.Set;

@Builder
@Schema(description = "Authentication and user registration request payload")
public record AuthRequest(
        @Schema(description = "Username for authentication or registration", example = "johndoe")
        String username,

        @Schema(description = "Password for authentication or registration", example = "password123")
        String password,

        @Schema(description = "Assigned user roles (e.g. ROLE_USER, ROLE_ADMIN)", example = "[\"ROLE_USER\"]")
        Optional<Set<String>> roles
) {
}
