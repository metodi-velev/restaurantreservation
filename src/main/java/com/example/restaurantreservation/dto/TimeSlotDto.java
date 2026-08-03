package com.example.restaurantreservation.dto;

import com.example.restaurantreservation.validator.ValidTimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Schema(description = "Details about a specific time slot for a reservation")
public record TimeSlotDto(
        @NotNull
        @Schema(description = "The date of the reservation", example = "2026-08-05")
        LocalDate date,

        @ValidTimeFormat
        @Schema(description = "The start time of the reservation (hourly format)", example = "18:00:00")
        LocalTime from,

        @ValidTimeFormat
        @Schema(description = "The end time of the reservation (hourly format)", example = "19:00:00")
        LocalTime to
) { }
