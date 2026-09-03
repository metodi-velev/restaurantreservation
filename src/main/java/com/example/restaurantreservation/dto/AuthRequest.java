package com.example.restaurantreservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Optional;
import java.util.Set;

@Builder
@Schema(description = "Authentication and user registration request payload")
public record AuthRequest(
        @Schema(description = "Username for authentication or registration", example = "johndoe")
        @NotBlank
        String username,

        @Schema(description = "Password for authentication or registration", example = "password123")
        @NotBlank
        String password,

        @Schema(description = "Assigned user roles (e.g. USER, ADMIN)", example = "[\"USER\"]")
        Optional<Set<String>> roles
) {
}
