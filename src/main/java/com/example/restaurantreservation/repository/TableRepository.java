package com.example.restaurantreservation.repository;

import com.example.restaurantreservation.entity.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<Table, Long> {
    @Query("SELECT DISTINCT t FROM Table t JOIN t.timeSlots ts WHERE " +
            "ts.reserved = false AND ts.date = :date AND ts.fromTime >= :fromTime AND ts.toTime <= :toTime AND " +
            "NOT EXISTS (SELECT 1 FROM TimeSlot ts2 WHERE ts2.table = t AND " +
            "ts2.reserved = true AND " +
            "ts2.date = :date AND " +
            "ts2.fromTime >= :fromTime AND " +
            "ts2.toTime <= :toTime)")
    List<Table> findAvailableTablesForTimeRange(
            @Param("date") LocalDate date,
            @Param("fromTime") LocalTime fromTime,
            @Param("toTime") LocalTime toTime
    );
}
