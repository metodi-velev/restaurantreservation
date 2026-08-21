package com.example.restaurantreservation.dto;

import com.example.restaurantreservation.validator.ValidTimeFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record ReservationDto(
        Long tableId,

        @NotNull
        LocalDate date,

        @ValidTimeFormat
        LocalTime fromTime,

        @ValidTimeFormat
        LocalTime toTime
) {
}
