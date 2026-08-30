package com.example.restaurantreservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Authentication response containing JWT token")
public record AuthResponse(
        @Schema(description = "JWT bearer token for authorization", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token
) {
}
