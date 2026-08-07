package com.example.restaurantreservation.repository;

import com.example.restaurantreservation.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    //@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT ts
        FROM TimeSlot ts
        WHERE ts.table.id = :tableId
          AND ts.date = :date
          AND ts.fromTime >= :from
          AND ts.toTime <= :to
          AND ts.reserved = false
        ORDER BY ts.fromTime
    """)
    List<TimeSlot> findForReservation(
            @Param("tableId") Long tableId,
            @Param("date") LocalDate date,
            @Param("from") LocalTime from,
            @Param("to") LocalTime to);
}
