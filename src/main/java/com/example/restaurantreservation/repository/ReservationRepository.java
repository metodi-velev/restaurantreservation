package com.example.restaurantreservation.repository;

import com.example.restaurantreservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findReservationByTableIdAndDateAndFromTimeAndToTime(
            Long tableId,
            LocalDate date,
            LocalTime fromTime,
            LocalTime toTime
    );

    List<Reservation> findReservationsByTableId(Long tableId);
}
