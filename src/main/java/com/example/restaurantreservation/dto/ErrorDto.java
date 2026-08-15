package com.example.restaurantreservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Error response details")
public record ErrorDto(
        @Schema(description = "Error code", example = "INVALID_PARTY_SIZE")
        String code,
        @Schema(description = "Descriptive error message", example = "Party size must be between 1 and 40.")
        String message,

        int status
) {
}
