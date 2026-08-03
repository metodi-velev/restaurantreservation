package com.example.restaurantreservation.dto;

import com.example.restaurantreservation.validator.ValidPartySize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
@Schema(description = "Request object for creating a new table reservation")
public record ReservationRequest(

        @Valid
        @Schema(description = "The requested time slot for the reservation")
        TimeSlotDto timeSlotDto,

        @NotNull
        @ValidPartySize
        @Positive(message = "Party size must be greater than zero.")
        @Schema(description = "Number of people in the party", example = "4")
        Integer partySize
) {
}
