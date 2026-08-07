package com.example.restaurantreservation.service;

import com.example.restaurantreservation.entity.TimeSlot;
import com.example.restaurantreservation.repository.TimeSlotRepository;
import lombok.extern.slf4j.Slf4j;
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

    public TimeSlotReservationService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

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
            throw new RuntimeException("No available time slots found.");
        }

        if (slots.stream().anyMatch(TimeSlot::isReserved)) {
            throw new RuntimeException("Time slot already reserved.");
        }

        slots.forEach(slot -> slot.setReserved(true));

        timeSlotRepository.saveAll(slots);

        timeSlotRepository.flush();

        log.info("Table {} reserved for party on {} at {} - {}",
                tableId, date, from, to);
    }
}
