package com.example.restaurantreservation.service;

import com.example.restaurantreservation.dto.ReservationResponseWithPictureWithEndpoint;
import com.example.restaurantreservation.entity.Reservation;
import com.example.restaurantreservation.entity.TimeSlot;
import com.example.restaurantreservation.exception.TimeSlotAlreadyReservedException;
import com.example.restaurantreservation.exception.TimeSlotNotFoundException;
import com.example.restaurantreservation.repository.ReservationRepository;
import com.example.restaurantreservation.repository.TimeSlotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
public class TimeSlotReservationService {

    private final TimeSlotRepository timeSlotRepository;
    private final ReservationRepository reservationRepository;

    public TimeSlotReservationService(TimeSlotRepository timeSlotRepository,
                                      ReservationRepository reservationRepository) {
        this.timeSlotRepository = timeSlotRepository;
        this.reservationRepository = reservationRepository;
    }

    @Retryable(
            retryFor = {TimeSlotNotFoundException.class, TimeSlotAlreadyReservedException.class,
                    OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 1.5)
    )
    @Transactional(propagation = Propagation.MANDATORY)
    public void reserveTimeSlots(Long tableId, LocalDate date, LocalTime from, LocalTime to) {
/*        timeSlotRepository.findForReservation(
                tableId,
                date,
                from,
                to
        ).forEach(ts -> ts.setReserved(true));*/

        List<TimeSlot> slots = timeSlotRepository.findForReservation(
                tableId,
                date,
                from,
                to);

        if (slots.isEmpty()) {
            throw new TimeSlotNotFoundException("No available time slots found.");
        }

        if (slots.stream().anyMatch(TimeSlot::isReserved)) {
            throw new TimeSlotAlreadyReservedException("Time slot already reserved.");
        }

        slots.forEach(slot -> slot.setReserved(true));

        timeSlotRepository.saveAll(slots);

        timeSlotRepository.flush();

        reservationRepository.save(
                Reservation.builder()
                        .tableId(tableId)
                        .date(date)
                        .fromTime(from)
                        .toTime(to)
                        .build()
        );

        timeSlotRepository.flush();

        log.info("Table {} reserved for party on {} at {} - {}",
                tableId, date, from, to);
    }

    // ===== RECOVER METHODS =====

    @Recover
    public ReservationResponseWithPictureWithEndpoint recover(TimeSlotNotFoundException e,
                                                              Long tableId,
                                                              LocalDate date,
                                                              LocalTime from,
                                                              LocalTime to) {
        log.warn("Recover: TimeSlotNotFoundException for table={} on {} from {} to {} o'clock", tableId, date, from, to);
        throw new TimeSlotNotFoundException("No available time slots found. Please try a different time.");
    }

    @Recover
    public ReservationResponseWithPictureWithEndpoint recover(TimeSlotAlreadyReservedException e,
                                                              Long tableId,
                                                              LocalDate date,
                                                              LocalTime from,
                                                              LocalTime to) {
        log.warn("Recover: TimeSlotAlreadyReservedException for table={} on {} from {} to {} o'clock", tableId, date, from, to);
        throw new TimeSlotAlreadyReservedException("Time slot already reserved. Please try a different time.");
    }

    @Recover
    public ReservationResponseWithPictureWithEndpoint recover(OptimisticLockingFailureException e,
                                                              Long tableId,
                                                              LocalDate date,
                                                              LocalTime from,
                                                              LocalTime to) {
        log.warn("Recover: OptimisticLockingFailureException for table={} on {} from {} to {} o'clock", tableId, date, from, to);
        throw new OptimisticLockingFailureException("Unable to reserve table due to concurrent modification. Please try again.");
    }

    @Recover
    public ReservationResponseWithPictureWithEndpoint recover(Exception e,
                                                              Long tableId,
                                                              LocalDate date,
                                                              LocalTime from,
                                                              LocalTime to) {
        log.error("Recover: Generic exception for table={} on {} from {} to {} o'clock", tableId, date, from, to, e);
        throw new RuntimeException("Unable to process reservation. Please try again later.");
    }
}
