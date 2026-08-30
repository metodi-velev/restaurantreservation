package com.example.restaurantreservation.dto;

import lombok.Builder;

import java.util.Optional;
import java.util.Set;

@Builder
public record AuthRequest(
        String username,
        String password,
        Optional<Set<String>> roles
) {
}
