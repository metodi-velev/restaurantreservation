package com.example.restaurantreservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Binary picture payload wrapper")
public record PicturePayload(
        @Schema(description = "Binary picture data")
        byte[] payload
) {
}
