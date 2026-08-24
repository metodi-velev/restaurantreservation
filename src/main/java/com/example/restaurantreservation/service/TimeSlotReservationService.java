package com.example.restaurantreservation.service;

import com.example.restaurantreservation.entity.Reservation;
import com.example.restaurantreservation.entity.TimeSlot;
import com.example.restaurantreservation.entity.User;
import com.example.restaurantreservation.exception.TimeSlotAlreadyReservedException;
import com.example.restaurantreservation.exception.TimeSlotNotFoundException;
import com.example.restaurantreservation.repository.ReservationRepository;
import com.example.restaurantreservation.repository.TimeSlotRepository;
import com.example.restaurantreservation.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
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
    private final UserRepository userRepository;
    private final UserService userService;

    public TimeSlotReservationService(TimeSlotRepository timeSlotRepository,
                                      ReservationRepository reservationRepository,
                                      UserRepository userRepository,
                                      UserService userService) {
        this.timeSlotRepository = timeSlotRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.userService = userService;
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

        User user = userService.getCurrentUser();

        slots.forEach(slot -> {
            slot.setReserved(true);
            slot.setReservedBy(user);
        });

        timeSlotRepository.saveAll(slots);

        timeSlotRepository.flush();

        reservationRepository.save(
                Reservation.builder()
                        .tableId(tableId)
                        .date(date)
                        .fromTime(from)
                        .toTime(to)
                        .reservedBy(user)
                        .build()
        );

        timeSlotRepository.flush();

        log.info("Table {} reserved for party on {} at {} - {}",
                tableId, date, from, to);
    }
}
