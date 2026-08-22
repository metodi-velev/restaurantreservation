package com.example.restaurantreservation.service;

import com.example.restaurantreservation.dto.ReservationDto;
import com.example.restaurantreservation.dto.TimeSlotDto;
import com.example.restaurantreservation.entity.Picture;
import com.example.restaurantreservation.entity.Reservation;
import com.example.restaurantreservation.entity.Table;
import com.example.restaurantreservation.entity.TimeSlot;
import com.example.restaurantreservation.exception.ReservationNotFoundException;
import com.example.restaurantreservation.exception.TimeSlotAlreadyReservedException;
import com.example.restaurantreservation.exception.TimeSlotNotFoundException;
import com.example.restaurantreservation.mapper.ReservationMapper;
import com.example.restaurantreservation.repository.PictureRepository;
import com.example.restaurantreservation.repository.ReservationRepository;
import com.example.restaurantreservation.repository.TableRepository;
import com.example.restaurantreservation.repository.TimeSlotRepository;
import jakarta.annotation.PostConstruct;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
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
@Slf4j
public class RestaurantReservationService {

    private static final int[] TABLE_CAPACITIES = {4, 6, 8, 12, 16, 20, 26, 30, 36, 40};
    private static final int NUMBER_OF_TABLES = TABLE_CAPACITIES.length;
    private static final int TIME_SLOT_START_HOUR = 10;
    private static final int TIME_SLOT_END_HOUR = 23;

    private final TableRepository tableRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotReservationService timeSlotReservationService;
    private final PictureRepository pictureRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ConcurrentHashMap<String, ReentrantLock> tableLocks = new ConcurrentHashMap<>();

    public RestaurantReservationService(TableRepository tableRepository,
                                        TimeSlotRepository timeSlotRepository,
                                        TimeSlotReservationService timeSlotReservationService,
                                        PictureRepository pictureRepository,
                                        ReservationRepository reservationRepository,
                                        ReservationMapper reservationMapper) {
        this.tableRepository = tableRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.timeSlotReservationService = timeSlotReservationService;
        this.pictureRepository = pictureRepository;
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
    }

    @Transactional
    @Retryable(
            retryFor = {TimeSlotNotFoundException.class, TimeSlotAlreadyReservedException.class,
                    OptimisticLockingFailureException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 50, multiplier = 1.5)
    )
    //@Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long reserveTable(Integer partySize, TimeSlotDto timeSlot) {

        validateTimeslot(timeSlot);

        Table tableEntity = tableRepository.findAvailableTablesForTimeRange(
                        partySize, timeSlot.date(), timeSlot.from(), timeSlot.to()
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("There is no table which suits your search criteria."));

        //reserveTimeSlots(tableEntity.getId(), timeSlot.date(), timeSlot.from(), timeSlot.to());
        timeSlotReservationService.reserveTimeSlots(tableEntity.getId(), timeSlot.date(), timeSlot.from(), timeSlot.to());

        return tableEntity.getId();
    }

    private void reserveTimeSlots(Long tableId, LocalDate date, LocalTime from, LocalTime to) {
        timeSlotRepository.findForReservation(
                tableId,
                date,
                from,
                to
        ).forEach(ts -> ts.setReserved(true));

        log.info("Table {} reserved for party on {} at {} - {}",
                tableId, date, from, to);
    }

    @Recover
    public Long recoverOptimisticLockingFailure(OptimisticLockingFailureException e,
                                                Integer partySize,
                                                TimeSlotDto timeSlot) {
        log.warn("All retries failed for partySize: {}, timeSlot: {}", partySize, timeSlot);
        throw new OptimisticLockingFailureException("Unable to reserve table after multiple attempts", e);
    }

    /**
     * Recovery method for TimeSlotNotFoundException
     * IMPORTANT: Must have the SAME parameters as the retryable method,
     * with the exception as the FIRST parameter, and the SAME return type
     */
    @Recover
    public Long recover(TimeSlotNotFoundException e, Integer partySize, TimeSlotDto timeSlot) {
        log.warn("All retry attempts exhausted for TimeSlotNotFoundException: partySize={}, timeSlot={}",
                partySize, timeSlot);
        // Re-throw the original exception (or a custom one)
        throw new TimeSlotNotFoundException("No available time slots found. Please try a different time.");
    }

    @Recover
    public Long recover(TimeSlotAlreadyReservedException e, Integer partySize, TimeSlotDto timeSlot) {
        log.warn("All retry attempts exhausted for TimeSlotAlreadyReservedException: partySize={}, timeSlot={}",
                partySize, timeSlot);
        // Re-throw the original exception (or a custom one)
        throw new TimeSlotAlreadyReservedException("Time slot already reserved. Please try a different time.");
    }

    /**
     * Recovery method for OptimisticLockingFailureException
     */
    @Recover
    public Long recover(OptimisticLockingFailureException e, Integer partySize, TimeSlotDto timeSlot) {
        log.warn("All retry attempts exhausted for OptimisticLockingFailure: partySize={}, timeSlot={}",
                partySize, timeSlot);
        throw new OptimisticLockingFailureException("Unable to reserve table due to concurrent modification. Please try again.");
    }

    /**
     * Generic recovery for any other exception
     */
    @Recover
    public Long recover(Exception e, Integer partySize, TimeSlotDto timeSlot) {
        log.error("All retry attempts exhausted: {}", e.getMessage(), e);
        throw new RuntimeException("Unable to process reservation. Please try again later.");
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

    public void cancelReservation(Long tableId, TimeSlotDto timeSlot) {

        Reservation reservation = reservationRepository.findReservationByTableIdAndDateAndFromTimeAndToTime(
                tableId,
                timeSlot.date(),
                timeSlot.from(),
                timeSlot.to()
        ).orElseThrow(() -> new ReservationNotFoundException(
                String.format("There exists no (single) reservation for table id %s on %s from %s to %s o'clock",
                        tableId,
                        timeSlot.date(),
                        timeSlot.from(),
                        timeSlot.to())
        ));

        reservationRepository.delete(reservation);

        tableRepository.findTableWithReservedTimeSlotsForTimeRange(
                tableId,
                timeSlot.date(),
                timeSlot.from(),
                timeSlot.to()
        ).orElseThrow(() -> new RuntimeException("There is no table which suits your search criteria."));

        List<TimeSlot> reservedTimeSlots = timeSlotRepository
                .findReservedTimeSlots(
                        tableId,
                        timeSlot.date(),
                        timeSlot.from(),
                        timeSlot.to()
                );
        reservedTimeSlots.forEach(reservedTimeSlot -> reservedTimeSlot.setReserved(false));

/*        Optional<Table> reservedTable = tables.stream()
                .filter(table -> table.getId().equals(tableId))
                .findFirst();

        reservedTable.orElseThrow(() -> new RuntimeException("Table not found"));

        Table table = reservedTable.get();

        table.setReserved(false);*/
    }

    @PostConstruct
    @Transactional
    public void initTables() {
        if (tableRepository.count() > 0) {
            log.info("Tables already initialized, skipping...");
            return;
        }

        List<Table> tables = createTables();
        tableRepository.saveAll(tables);

        validateTables(tables);

        log.info("Successfully initialized {} tables", tables.size());
    }

    private List<Table> createTables() {
        return IntStream.range(0, NUMBER_OF_TABLES)
                .mapToObj(this::createTable)
                .toList();
    }

    private Table createTable(int index) {
        Table table = Table.builder()
                .tableName("Table " + (index + 1))
                .capacity(TABLE_CAPACITIES[index])
                .build();

        // Add time slots
        initializeTimeSlots().forEach(table::addTimeSlot);

        // Add picture
        Picture picture = createPicture(index + 1);
        table.setPicture(picture);
        picture.setTable(table);

        return table;
    }

    private Picture createPicture(int tableNumber) {
        return Picture.builder()
                .name("Table " + tableNumber + " Picture")
                .imageData(initializeImage(tableNumber))
                .build();
    }

    private List<TimeSlot> initializeTimeSlots() {
        List<TimeSlot> timeSlots = new ArrayList<>();
        LocalDate startDate = LocalDate.now();

        // Generate time slots for the next 14 days
        for (int dayOffset = 0; dayOffset < 14; dayOffset++) {
            LocalDate date = startDate.plusDays(dayOffset);
            int startHour = getStartHour(dayOffset);

            for (int hour = startHour; hour < TIME_SLOT_END_HOUR; hour++) {
                timeSlots.add(createTimeSlot(date, hour));
            }
        }

        return timeSlots;
    }

    private int getStartHour(int dayOffset) {
        int currentHour = LocalTime.now().getHour();
        if (dayOffset == 0 && currentHour > TIME_SLOT_START_HOUR) {
            return currentHour + 1;
        }
        return TIME_SLOT_START_HOUR;
    }

    private TimeSlot createTimeSlot(LocalDate date, int hour) {
        return TimeSlot.builder()
                .date(date)
                .fromTime(LocalTime.of(hour, 0))
                .toTime(LocalTime.of(hour + 1, 0))
                .build();
    }

    private byte[] initializeImage(int tableNumber) {
        try {
            String imagePath = String.format("images/table_%d_picture.jpg", tableNumber);
            Resource resource = new ClassPathResource(imagePath);

            if (!resource.exists()) {
                log.warn("Image not found for table {}: {}", tableNumber, imagePath);
                return new byte[0];
            }

            return resource.getInputStream().readAllBytes();
        } catch (IOException | InvalidPathException e) {
            log.warn("Could not load image for table {}: {}", tableNumber, e.getMessage());
            // Return a default placeholder image or null
            return new byte[0];
        }
    }

    private void validateTables(List<Table> tables) {
        if (tables == null || tables.size() != NUMBER_OF_TABLES) {
            throw new RuntimeException(String.format(
                    "Expected %d tables, but found %d",
                    NUMBER_OF_TABLES,
                    tables != null ? tables.size() : 0
            ));
        }
        log.info("Validation successful: {} tables initialized", tables.size());
    }

    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAll()
                .stream().map(reservationMapper::reservationToReservationDtoMapper).toList();
    }

    public List<ReservationDto> getReservationsForTableId(Long tableId) {
        return reservationRepository.findReservationsByTableId(tableId)
                .stream().map(reservationMapper::reservationToReservationDtoMapper).toList();
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
