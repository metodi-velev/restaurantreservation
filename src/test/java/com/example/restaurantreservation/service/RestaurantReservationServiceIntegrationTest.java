package com.example.restaurantreservation.service;

import com.example.restaurantreservation.dto.TimeSlotDto;
import com.example.restaurantreservation.entity.TimeSlot;
import com.example.restaurantreservation.repository.PictureRepository;
import com.example.restaurantreservation.repository.ReservationRepository;
import com.example.restaurantreservation.repository.TableRepository;
import com.example.restaurantreservation.repository.TimeSlotRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RestaurantReservationServiceIntegrationTest {

    @Autowired
    private RestaurantReservationService reservationService;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private PictureRepository pictureRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private final AtomicInteger successfulCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);

/*    @BeforeEach
    void setUpDatabase() {
        reservationService.initTables();
    }*/

    @AfterEach
    void cleanDatabase() {
        reservationRepository.deleteAllInBatch();
        timeSlotRepository.deleteAllInBatch();
        pictureRepository.deleteAllInBatch();
        tableRepository.deleteAllInBatch();
    }

    @Test
    void shouldPreventDoubleBooking() throws Exception {

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {

            Callable<Long> task = () -> {

                ready.countDown();
                start.await();

                return reservationService.reserveTable(
                        4,
                        new TimeSlotDto(
                                LocalDate.now().plusDays(1),
                                LocalTime.of(18, 0),
                                LocalTime.of(20, 0)
                        )
                );
            };

            Future<Long> first = executor.submit(task);
            Future<Long> second = executor.submit(task);

            ready.await();
            start.countDown();

            int successfulReservations = 0;

            Long firstResult = first.get(5, TimeUnit.SECONDS);
            successfulReservations++;

            Long secondResult = second.get(5, TimeUnit.SECONDS);
            successfulReservations++;

            assertEquals(2, successfulReservations);

            assertNotEquals(firstResult, secondResult,
                    "Same table should not be assigned to both threads!");

            //assertTrue(List.of(1L, 2L).contains(firstResult));
            //assertTrue(List.of(1L, 2L).contains(secondResult));

            assertNotNull(firstResult);
            assertNotNull(secondResult);

            List<TimeSlot> reservedSlots =
                    timeSlotRepository.findByReservedAndDateAndFromTimeGreaterThanEqualAndToTimeLessThanEqual(
                            true,
                            LocalDate.now().plusDays(1),
                            LocalTime.of(18, 0),
                            LocalTime.of(20, 0));

            // Should have 4 reserved slots (2 slots per table: 18:00-19:00 and 19:00-20:00)
            assertEquals(4, reservedSlots.size(),
                    "Expected 4 reserved slots (2 per table, 2 tables)");
        }
    }

    @Test
    void shouldThrowOptimisticLockingFailureException_whenConcurrentModification() throws Exception {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime from = LocalTime.of(18, 0);
        LocalTime to = LocalTime.of(20, 0);
        TimeSlotDto timeSlot = new TimeSlotDto(date, from, to);
        Integer partySize = 4;
        int threadCount = 5;

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        // WHEN
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            IntStream.range(0, threadCount).forEach(i -> executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    assertThrows(OptimisticLockingFailureException.class, () -> {
                        System.out.println("OptimisticLockingFailureException is thrown.");
                        reservationService.reserveTable(partySize, timeSlot);
                    });
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }));

            ready.await();
            start.countDown();
        }
    }

    @Test
    void shouldRetryOnOptimisticLockingFailure() throws Exception {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime from = LocalTime.of(18, 0);
        LocalTime to = LocalTime.of(20, 0);
        TimeSlotDto timeSlot = new TimeSlotDto(date, from, to);
        Integer partySize = 4;

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {

            // First task - will succeed
            Runnable task1 = () -> {
                try {
                    ready.countDown();
                    start.await();
                    Long result = reservationService.reserveTable(partySize, timeSlot);
                    System.out.println("Task 1: Table " + result + " reserved");
                } catch (Exception e) {
                    System.err.println("Task 1 failed: " + e.getMessage());
                    e.printStackTrace();
                }
            };

            // Second task - may fail with OptimisticLockingFailureException
            // and should reserve the next free table after that
            Runnable task2 = () -> {
                try {
                    ready.countDown();
                    start.await();
                    Long result = reservationService.reserveTable(partySize, timeSlot);
                    System.out.println("Task 2: Table " + result + " reserved");
                } catch (OptimisticLockingFailureException e) {
                    System.out.println("Task 2: OptimisticLockingFailureException thrown!");
                    throw new RuntimeException("Expected OptimisticLockingFailureException", e);
                } catch (Exception e) {
                    System.err.println("Task 2 failed: " + e.getMessage());
                    e.printStackTrace();
                }
            };

            executor.submit(task1);
            executor.submit(task2);

            ready.await();
            start.countDown();
        }

        // Verify that the two tables was reserved
        List<TimeSlot> reservedSlots = timeSlotRepository
                .findByReservedAndDateAndFromTimeGreaterThanEqualAndToTimeLessThanEqual(
                        true, date, from, to);

        // Should have 4 slots (2 table × 2 time slots: 18-19 and 19-20)
        assertThat(reservedSlots).hasSize(4);
        assertThat(reservedSlots).allMatch(TimeSlot::isReserved);
    }

/*    @Test
    void shouldCallRecoverWhenOptimisticLockingFailureExhausted() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime from = LocalTime.of(18, 0);
        LocalTime to = LocalTime.of(20, 0);
        TimeSlotDto timeSlot = new TimeSlotDto(date, from, to);
        Integer partySize = 4;

        // Mock or simulate repeated failures
        assertThrows(RuntimeException.class, () -> {
            try (ExecutorService executor = Executors.newFixedThreadPool(20)) {
                for (int i = 0; i < 20; i++) {
                    executor.submit(() -> reservationService.reserveTable(partySize, timeSlot));
                }
            }
        });
    }*/

    @Test
    void shouldHandleConcurrentReservationsSuccessfully() throws Exception {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime from = LocalTime.of(18, 0);
        LocalTime to = LocalTime.of(20, 0);
        TimeSlotDto timeSlot = new TimeSlotDto(date, from, to);
        Integer partySize = 4;

        int threadCount = 20;
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        List<Long> results = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        ready.countDown();
                        start.await();
                        Long tableId = reservationService.reserveTable(partySize, timeSlot);
                        synchronized (results) {
                            results.add(tableId);
                        }
                    } catch (Exception e) {
                        synchronized (errors) {
                            errors.add(e);
                        }
                    }
                });
            }

            ready.await();
            start.countDown();

            // Wait for all tasks to complete
            Thread.sleep(5000);

            // Then: Verify results
            System.out.println("Successful reservations: " + results.size());
            System.out.println("Errors: " + errors.size());

            // Assert that we have at least 2 successful reservations
            // (with party size 4, the restaurant should have multiple available tables)
            assertThat(results.size()).isGreaterThanOrEqualTo(2);

            // Assert all table IDs are unique (no double booking)
            assertThat(results).doesNotHaveDuplicates();

            // Verify the total number of reserved slots matches
            int expectedSlots = results.size() * 2; // 2 slots per table (18-19, 19-20)
            List<TimeSlot> reservedSlots = timeSlotRepository
                    .findByReservedAndDateAndFromTimeGreaterThanEqualAndToTimeLessThanEqual(
                            true, date, from, to);

            assertThat(reservedSlots).hasSize(expectedSlots);
        }
    }

    @Test
    void shouldHandleHighConcurrencyLoad() throws Exception {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime from = LocalTime.of(18, 0);
        LocalTime to = LocalTime.of(20, 0);
        TimeSlotDto timeSlot = new TimeSlotDto(date, from, to);
        Integer partySize = 4;

        int numberOfRequests = 20;
        CountDownLatch ready = new CountDownLatch(numberOfRequests);
        CountDownLatch start = new CountDownLatch(1);

        List<Long> reservedTables = new ArrayList<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(numberOfRequests)) {

            for (int i = 0; i < numberOfRequests; i++) {
                executor.submit(() -> {
                    try {
                        ready.countDown();
                        start.await();
                        Long tableId = reservationService.reserveTable(partySize, timeSlot);
                        synchronized (reservedTables) {
                            reservedTables.add(tableId);
                        }
                        successfulCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        System.out.println("Failed: " + e.getMessage());
                    }
                });
            }

            ready.await();
            start.countDown();
            //Thread.sleep(10000);

        }
        // Then
        System.out.println("Successful: " + successfulCount.get());
        System.out.println("Failed: " + failureCount.get());
        System.out.println("Total reservations: " + reservedTables.size());

        // Assertions
        assertThat(reservedTables).hasSize(successfulCount.get());
        assertThat(reservedTables).doesNotHaveDuplicates();

        // Each table has 2 time slots (18-19 and 19-20)
        assertThat(reservedTables.size() * 2)
                .isEqualTo(timeSlotRepository
                        .findByReservedAndDateAndFromTimeGreaterThanEqualAndToTimeLessThanEqual(
                                true, date, from, to)
                        .size());
    }

    @Test
    void shouldHandleConcurrentReservationsWithoutDoubleBooking() throws Exception {
        // GIVEN
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime from = LocalTime.of(18, 0);
        LocalTime to = LocalTime.of(20, 0);
        TimeSlotDto timeSlot = new TimeSlotDto(date, from, to);
        int partySize = 4;
        int threadCount = 20;

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successful = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        CopyOnWriteArrayList<Long> results = new CopyOnWriteArrayList<>();
        ConcurrentHashMap<Integer, Long> tableIdMap = new ConcurrentHashMap<>();

        // WHEN
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            IntStream.range(0, threadCount).forEach(i -> executor.submit(() -> {
                try {
                    ready.countDown();
                    start.await();
                    Long tableId = reservationService.reserveTable(partySize, timeSlot);
                    //synchronized (results) {
                    results.add(tableId);
                    //}
                    tableIdMap.put(i, tableId);
                    successful.incrementAndGet();
                } catch (OptimisticLockingFailureException e) {
                    failed.incrementAndGet();
                    System.out.println("OptimisticLockingFailureException: " + e.getMessage());
                } catch (Exception e) {
                    failed.incrementAndGet();
                    System.out.println("Other exception: " + e.getMessage());
                }
            }));

            ready.await();
            start.countDown();
            //executor.shutdown();
            //executor.awaitTermination(10, TimeUnit.SECONDS);
        }
        // THEN
        System.out.println("Successful reservations: " + successful.get());
        System.out.println("Failed reservations: " + failed.get());

        // Assert
        assertThat(results).doesNotHaveDuplicates();
        assertThat(tableIdMap.values()).doesNotHaveDuplicates();
        assertThat(results.size()).isGreaterThanOrEqualTo(2);

        // Verify no double booking
        List<TimeSlot> reservedSlots = timeSlotRepository
                .findByReservedAndDateAndFromTimeGreaterThanEqualAndToTimeLessThanEqual(
                        true, date, from, to);
        assertThat(reservedSlots.size()).isEqualTo(results.size() * 2);
        assertThat(reservedSlots).allMatch(TimeSlot::isReserved);
    }
}