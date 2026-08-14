package com.example.restaurantreservation.dto;

import com.example.restaurantreservation.entity.Picture;
import com.example.restaurantreservation.serializer.PictureSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ReservationResponseWithPicture(
        @Schema(description = "The unique identifier of the reserved table", example = "1")
        Long tableId,

        @JsonSerialize(using = PictureSerializer.class)
        @Schema(description = "Picture of the table")
        Picture image
) {
}
