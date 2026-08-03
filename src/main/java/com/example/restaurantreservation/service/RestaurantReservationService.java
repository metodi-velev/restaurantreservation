package com.example.restaurantreservation.service;

import com.example.restaurantreservation.dto.TimeSlotDto;
import com.example.restaurantreservation.entity.Table;
import com.example.restaurantreservation.entity.TimeSlot;
import com.example.restaurantreservation.repository.TableRepository;
import com.example.restaurantreservation.repository.TimeSlotRepository;
import jakarta.annotation.PostConstruct;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <h2>Stage 1: Basic Reservation ♠</h2>
 *
 * <p>Create a restaurant reservation system.</p>
 *
 * <p>
 * The restaurant has <strong>10 tables</strong> with different capacities.
 * </p>
 *
 * <p><strong>Functionality:</strong></p>
 * <ol>
 *     <li>
 *         Add an ability to reserve a table by providing party size and time slot
 *         (hourly slots only, e.g., 18:00, 19:00, 20:00).
 *         Returns the reserved table ID.
 *     </li>
 *     <li>
 *         Add an ability to cancel a reservation by providing table ID and time slot.
 *     </li>
 * </ol>
 *
 * <p>
 * Each table can only have one reservation per time slot.
 * Always assign the smallest available table that fits the party.
 * </p>
 */
@Builder
@Getter
@Setter
@Transactional(rollbackFor = {IllegalArgumentException.class, RuntimeException.class})
@Service
public class RestaurantReservationService {

    private final TableRepository tableRepository;
    private final TimeSlotRepository timeSlotRepository;

    public RestaurantReservationService(TableRepository tableRepository,
                                        TimeSlotRepository timeSlotRepository) {
        this.tableRepository = tableRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    public Long reserveTable(Integer partySize, TimeSlotDto timeSlot) {

        validateTimeslot(timeSlot);

        Optional<Table> tableEntity = tableRepository.findAvailableTablesForTimeRange(
                        timeSlot.date(), timeSlot.from(), timeSlot.to()
                )
                .stream()
                .filter(table ->
                        partySize <= table.getCapacity()
                )
                .min(Comparator.comparing(Table::getCapacity));

        tableEntity.ifPresent(table -> {
            reserveTimeSlots(table.getTimeSlots(), timeSlot.date(), timeSlot.from(), timeSlot.to());
        });

        return tableEntity.map(Table::getId).orElseThrow(() ->
                new RuntimeException("There is no table which" +
                        " suits your search criteria."));
    }

    private void reserveTimeSlots(List<TimeSlot> timeSlots, LocalDate date, LocalTime from, LocalTime to) {
        timeSlots.stream()
                .filter(ts -> ts.getDate().equals(date) &&
                        !ts.isReserved() && !ts.getFromTime().isBefore(from) && !ts.getToTime().isAfter(to))
                .forEach(ts -> ts.setReserved(true));
    }

    private static void validateTimeslot(TimeSlotDto timeSlot) {
        if (timeSlot.from().getMinute() != 0 || timeSlot.to().getMinute() != 0) {
            throw new IllegalArgumentException("Invalid time slot. Only hourly slots are allowed, e.g., 18:00, 19:00, 20:00.");
        }

        if (timeSlot.to().isBefore(timeSlot.from()) || timeSlot.to().equals(timeSlot.from())) {
            throw new IllegalArgumentException("Invalid time slot. 'From'"
                    + timeSlot.from()
                    + " time must be before 'to'"
                    + timeSlot.to()
                    + " time.");
        }

        StringBuilder invalidHours = getInvalidHoursMessage(timeSlot);
        if (!invalidHours.isEmpty()) {
            throw new IllegalArgumentException(invalidHours.toString());
        }
    }

    private static StringBuilder getInvalidHoursMessage(TimeSlotDto timeSlot) {
        int fromHour = timeSlot.from().getHour();
        int toHour = timeSlot.to().getHour();

        StringBuilder invalidHours = new StringBuilder();

        if (fromHour < 10 || fromHour > 22) {
            invalidHours.append("The 'from' hour ")
                    .append(fromHour)
                    .append(" is invalid. It should be between 10 and 22.");
        }

        if (toHour < 11 || toHour > 23) {
            invalidHours.append("\n");
            invalidHours.append("The 'to' hour ")
                    .append(toHour)
                    .append(" is invalid. It should be between 11 and 23.");
        }
        return invalidHours;
    }

    public void cancelReservation(Long tableId, LocalTime timeSlot) {
/*        Optional<Table> reservedTable = tables.stream()
                .filter(table -> table.getId().equals(tableId))
                .findFirst();

        reservedTable.orElseThrow(() -> new RuntimeException("Table not found"));

        Table table = reservedTable.get();

        table.setReserved(false);*/
    }

    @PostConstruct
    private List<Table> initTables() {
        int[] capacities = {4, 6, 8, 12, 16, 20, 26, 30, 36, 40};

        List<Table> tables = IntStream.range(0, capacities.length)
                .mapToObj(i -> new Table(
                        "Table " + (i + 1),
                        capacities[i]
                ))
                .collect(Collectors.toList());

        for (Table table : tables) {
            List<TimeSlot> timeSlots = initializeTimeslots();
            timeSlots.forEach(table::addTimeSlot);
            tableRepository.save(table);
        }

        validateTables(tables);

        return tables;
    }

    public void validateTables(List<Table> tables) {
        if (tables == null || tables.size() != 10) {
            throw new RuntimeException(
                    String.format("The restaurant reservation tables don't match! Size: %s", (tables == null ? 0 : tables.size())));
        }
    }

    protected List<TimeSlot> initializeTimeslots() {
        List<TimeSlot> timeSlots = new ArrayList<>();

        for (int i = 0; i < 14; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            int start = 10;
            int currentHour = LocalTime.now().getHour();
            if (i == 0 && currentHour > 10) {
                start = currentHour + 1;
            }
            for (int j = start; j < 23; j++) {
                LocalTime from = LocalTime.of(j, 0);
                LocalTime to = LocalTime.of(j + 1, 0);
                timeSlots.add(TimeSlot.builder()
                        .date(date)
                        .fromTime(from)
                        .toTime(to)
                        .build());
            }
        }

        return timeSlots;
    }
}

/*	Stage 1: Basic Reservation♠

	Create a restaurant reservation system.

	The restaurant has 10 tables with different capacities.
	Functionality:
			1)Add an ability to reserve a table by providing party size and time slot
			     (hourly slots only, e.g., 18:00, 19:00, 20:00). Returns the reserved table ID.
 			2)Add an ability to cancel a reservation by providing table ID and time slot.
	Each table can only have one reservation per time slot. Always assign the smallest available table that fits the party.
*/

/*
    select * from tables tb
    inner join timeslots ts
    on tb.id = ts.table_id
    where tb.id = 1;
*/
