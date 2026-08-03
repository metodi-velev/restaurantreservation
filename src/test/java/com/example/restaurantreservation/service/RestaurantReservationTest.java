package com.example.restaurantreservation.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestaurantReservationTest {

/*    private RestaurantReservationService restaurantReservation;

    private List<Table> tables;

    @BeforeEach
    void setUp() {
        restaurantReservation = new RestaurantReservationService();
        initTables();
        tables = restaurantReservation.getTables();
    }

    @AfterAll
    static void tearDown() {
        System.gc();
    }

    @Test
    void reserveTable() {
        System.out.println("Size of the tables: " + restaurantReservation.getTables().size());
        Long tableID = restaurantReservation.reserveTable(16, LocalTime.of(14, 00));
        assertEquals(Long.valueOf(5L), tableID);

        IntStream.range(0, tables.size() - 1)
                .forEach(index -> {
                    tables.get(index).setReserved(true);
                });
        tableID = restaurantReservation.reserveTable(16, LocalTime.of(19, 00));
        assertEquals(Long.valueOf(10L), tableID);

        for(Table table : tables) {
            assertTrue(table.getReserved());
        }

        tables.get(5).setReserved(false);
        assertFalse(tables.get(5).getReserved());
        tableID = restaurantReservation.reserveTable(16, LocalTime.of(15, 00));
        assertEquals(Long.valueOf(6L), tableID);
        assertTrue(tables.get(5).getReserved());
    }

    @Test
    void validateTables_shouldThrowException_whenTablesSizeIsNot10() {
        restaurantReservation.getTables().removeIf(table -> table.getTableId() == 1L);
        System.out.println("Size of the tables: " + restaurantReservation.getTables().size());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> restaurantReservation.validateTables());
        assertInstanceOf(RuntimeException.class, exception);
        assertEquals(exception.getMessage(),
                "The restaurant reservation tables don't match! Size: " + tables.size());
    }

    @Test
    void reserveTable_shouldThrowException_whenTimeSlotInvalid() {
        assertThatThrownBy(() -> restaurantReservation.reserveTable(16, LocalTime.of(14, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid time slot. Only hourly slots are allowed, e.g., 18:00, 19:00, 20:00.");
    }

    @Test
    void reserveTable_shouldThrowException_whenNoTableAvailable_timeslot() {
        assertThatThrownBy(() -> restaurantReservation.reserveTable(2, LocalTime.of(20, 00)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(
                        "There is no free table for " + 2 + " people at "
                                + LocalTime.of(20, 00) + " o'clock. Please, select a different table which fits " + 2 + " people"
                                + " at " + LocalTime.of(20, 00) + " o'clock."
                );
    }

    @Test
    void reserveTable_shouldThrowException_whenNoTableAvailable_reserved() {
        for(Table table : tables) {
            table.setReserved(true);
        }
        assertThatThrownBy(() -> restaurantReservation.reserveTable(2, LocalTime.of(10, 00)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(
                        "There is no free table for " + 2 + " people at "
                                + LocalTime.of(10, 00) + " o'clock. Please, select a different table which fits " + 2 + " people"
                                + " at " + LocalTime.of(10, 00) + " o'clock."
                );
    }

    @Test
    void reserveTable_shouldThrowException_whenNoTableAvailable_partySize() {
        assertThatThrownBy(() -> restaurantReservation.reserveTable(65, LocalTime.of(19, 00)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(
                        "There is no free table for " + 65 + " people at "
                                + LocalTime.of(19, 00) + " o'clock. Please, select a different table which fits " + 65 + " people"
                                + " at " + LocalTime.of(19, 00) + " o'clock."
                );
    }

    @Test
    void cancelReservation() {

    }

    private void initTables() {
        int[] capacities = {2, 5, 10, 15, 20, 25, 30, 40, 50, 60};

        restaurantReservation.setTables(
                IntStream.range(0, capacities.length)
                        .mapToObj(i -> new Table(
                                (long) (i + 1),
                                "Table " + (i + 1),
                                LocalTime.of(10 + i, 0),
                                capacities[i],
                                false))
                        .collect(Collectors.toList()));

    }*/


}