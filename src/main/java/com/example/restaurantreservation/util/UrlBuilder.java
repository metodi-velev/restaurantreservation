package com.example.restaurantreservation.util;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.stereotype.Component;

@Component
public class UrlBuilder {

    private static final String IMAGE_ENDPOINT = "/api/images/table/";

    public String buildImageUrl(Long tableId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(IMAGE_ENDPOINT)
                .path(tableId.toString())
                .toUriString();
    }

    public String buildFullImageUrl(Long tableId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(IMAGE_ENDPOINT)
                .path(tableId.toString())
                .toUriString();
    }
}
