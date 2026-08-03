package com.example.restaurantreservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "Response containing details of a successful reservation")
public record ReservationResponse(
        @Schema(description = "The unique identifier of the reserved table", example = "1")
        Long tableId,

        @Schema(description = "The name or number of the reserved table", example = "Table 1")
        String tableName,

        @Valid
        @Schema(description = "List of all reservations for this table")
        List<TimeSlotDto> reservations
) {
}
