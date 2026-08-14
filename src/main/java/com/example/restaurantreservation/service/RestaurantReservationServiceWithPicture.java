package com.example.restaurantreservation.service;

import com.example.restaurantreservation.dto.ReservationResponseWithPictureWithEndpoint;
import com.example.restaurantreservation.dto.TimeSlotDto;
import com.example.restaurantreservation.entity.Picture;
import com.example.restaurantreservation.repository.PictureRepository;
import com.example.restaurantreservation.util.UrlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RestaurantReservationServiceWithPicture {

    private final RestaurantReservationService restaurantReservationService;
    private final PictureRepository pictureRepository;
    private final UrlBuilder urlBuilder;

    public RestaurantReservationServiceWithPicture(RestaurantReservationService restaurantReservationService,
                                                   PictureRepository pictureRepository,
                                                   UrlBuilder urlBuilder) {
        this.restaurantReservationService = restaurantReservationService;
        this.pictureRepository = pictureRepository;
        this.urlBuilder = urlBuilder;
    }

    @Transactional
    public ReservationResponseWithPictureWithEndpoint reserveTableWithPicture(Integer partySize, TimeSlotDto timeSlot) {

        Long tableId = restaurantReservationService.reserveTable(partySize, timeSlot);

        Picture picture = pictureRepository.findPictureByTableId(tableId).orElse(null);

        String imageUrl = picture != null
                ? urlBuilder.buildImageUrl(tableId)
                : null;

        return ReservationResponseWithPictureWithEndpoint.builder()
                .tableId(tableId)
                .imageUrl(imageUrl)
                .build();
    }
}