package com.example.restaurantreservation.dto;

import com.example.restaurantreservation.validator.ValidPartySize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record ReservationRequest(

        @Valid
        TimeSlotDto timeSlotDto,

        @NotNull
        @ValidPartySize
        @Positive(message = "Party size must be greater than zero.")
        Integer partySize
) {
}
