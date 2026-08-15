package com.example.restaurantreservation.service;

import com.example.restaurantreservation.dto.ReservationResponseWithPictureWithEndpoint;
import com.example.restaurantreservation.dto.TimeSlotDto;
import com.example.restaurantreservation.entity.Picture;
import com.example.restaurantreservation.exception.TimeSlotAlreadyReservedException;
import com.example.restaurantreservation.exception.TimeSlotNotFoundException;
import com.example.restaurantreservation.repository.PictureRepository;
import com.example.restaurantreservation.util.UrlBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
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

    /**
     * IMPORTANT: This method also needs @Retryable because it calls a retryable method.
     * The retry will happen here, and the Recover method in this class will handle it.
     */
    @Retryable(
            retryFor = {TimeSlotNotFoundException.class, TimeSlotAlreadyReservedException.class,
                    OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 1.5)
    )
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

    // ===== RECOVER METHODS =====

    @Recover
    public ReservationResponseWithPictureWithEndpoint recover(TimeSlotNotFoundException e,
                                                              Integer partySize,
                                                              TimeSlotDto timeSlot) {
        log.warn("Recover: TimeSlotNotFoundException for partySize={}", partySize);
        throw new TimeSlotNotFoundException("No available time slots found. Please try a different time.");
    }

    @Recover
    public ReservationResponseWithPictureWithEndpoint recover(TimeSlotAlreadyReservedException e,
                                                              Integer partySize,
                                                              TimeSlotDto timeSlot) {
        log.warn("Recover: TimeSlotAlreadyReservedException for partySize={}", partySize);
        throw new TimeSlotAlreadyReservedException("Time slot already reserved. Please try a different time.");
    }

    @Recover
    public ReservationResponseWithPictureWithEndpoint recover(OptimisticLockingFailureException e,
                                                              Integer partySize,
                                                              TimeSlotDto timeSlot) {
        log.warn("Recover: OptimisticLockingFailureException for partySize={}", partySize);
        throw new OptimisticLockingFailureException("Unable to reserve table due to concurrent modification. Please try again.");
    }

    @Recover
    public ReservationResponseWithPictureWithEndpoint recover(Exception e,
                                                              Integer partySize,
                                                              TimeSlotDto timeSlot) {
        log.error("Recover: Generic exception for partySize={}", partySize, e);
        throw new RuntimeException("Unable to process reservation. Please try again later.");
    }
}