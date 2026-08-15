package com.example.restaurantreservation.repository;

import com.example.restaurantreservation.entity.Table;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<Table, Long> {
    @Query("""
            SELECT t
            FROM Table t
            WHERE t.capacity >= :partySize
            AND NOT EXISTS (
                SELECT 1
                FROM TimeSlot ts
                WHERE ts.table = t
                  AND ts.date = :date
                  AND ts.fromTime >= :fromTime
                  AND ts.toTime <= :toTime
                  AND ts.reserved = true
            )
            ORDER BY t.capacity
            """)
    List<Table> findAvailableTablesForTimeRange(
            @Param("partySize") int partySize,
            @Param("date") LocalDate date,
            @Param("fromTime") LocalTime fromTime,
            @Param("toTime") LocalTime toTime
    );

    @Query("""
            SELECT t
            FROM Table t
            WHERE t.id = :tableId
            AND NOT EXISTS (
                SELECT 1
                FROM TimeSlot ts
                WHERE ts.table = t
                  AND ts.date = :date
                  AND ts.fromTime >= :fromTime
                  AND ts.toTime <= :toTime
                  AND ts.reserved = false
            )
            """)
    Optional<Table> findTableWithReservedTimeSlotsForTimeRange(
            @Param("tableId") Long tableId,
            @Param("date") LocalDate date,
            @Param("fromTime") LocalTime fromTime,
            @Param("toTime") LocalTime toTime
    );

    // Lock the table for update
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Table t WHERE t.id = :tableId")
    Optional<Table> findByIdForUpdate(@Param("tableId") Long tableId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT DISTINCT t FROM Table t WHERE " +
            "t.capacity >= :partySize AND " +
            "t.id NOT IN (SELECT ts.table.id FROM TimeSlot ts WHERE " +
            "ts.reserved = true AND " +
            "ts.date = :date AND " +
            "(ts.fromTime < :toTime AND ts.toTime > :fromTime)) " +
            "ORDER BY t.capacity")
    List<Table> findAvailableTablesForTimeRangeV2(
            @Param("partySize") int partySize,
            @Param("date") LocalDate date,
            @Param("fromTime") LocalTime fromTime,
            @Param("toTime") LocalTime toTime
    );

    @Query("""
            SELECT t
            FROM Table t JOIN FETCH t.picture pic
            WHERE t.id = :id
            """)
    Optional<Table> findTableById(@Param("id") Long id);
}
