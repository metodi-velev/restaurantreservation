package com.example.restaurantreservation.dto;

import com.example.restaurantreservation.entity.Picture;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Builder
@Schema(description = "Response containing reserved table ID and image endpoint URL")
public record ReservationResponseWithPictureWithEndpoint(
        @Schema(description = "The unique identifier of the reserved table", example = "1")
        Long tableId,

        @Schema(description = "URL to get the table image", example = "http://localhost:8080/api/images/table/1")
        String imageUrl
) {
    public static ReservationResponseWithPictureWithEndpoint from(Long tableId, Picture picture) {

        String imageUrl = picture != null
                ? ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/images/table/")
                .path(tableId.toString())
                .toUriString()
                : null;

        return ReservationResponseWithPictureWithEndpoint.builder()
                .tableId(tableId)
                .imageUrl(imageUrl)
                .build();
    }
}
