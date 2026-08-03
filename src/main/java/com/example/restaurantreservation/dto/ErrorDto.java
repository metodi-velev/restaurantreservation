package com.example.restaurantreservation.dto;

import lombok.Builder;

@Builder
public record ErrorDto(
        String code,
        String message
) {
}
