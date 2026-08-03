package com.example.restaurantreservation.repository;

import com.example.restaurantreservation.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByReservedAndDateAndFromTimeGreaterThanEqualAndToTimeLessThanEqual(
            boolean reserved,
            LocalDate date,
            LocalTime fromTime,
            LocalTime toTime
    );
}
