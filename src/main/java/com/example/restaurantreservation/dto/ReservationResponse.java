package com.example.restaurantreservation.dto;

import jakarta.validation.Valid;
import lombok.Builder;

import java.util.List;

@Builder
public record ReservationResponse(
        Long tableId,

        String tableName,

        @Valid
        List<TimeSlotDto> reservations
) {
}
