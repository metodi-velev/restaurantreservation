package com.example.restaurantreservation.dto;

import lombok.Builder;

@Builder
public record PicturePayload(
        byte[] payload
) {
}
