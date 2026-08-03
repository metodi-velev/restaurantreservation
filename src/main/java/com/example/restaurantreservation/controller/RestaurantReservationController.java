package com.example.restaurantreservation.controller;

import com.example.restaurantreservation.dto.ReservationRequest;
import com.example.restaurantreservation.entity.TimeSlot;
import com.example.restaurantreservation.entity.Table;
import com.example.restaurantreservation.service.RestaurantReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║   🍽️  Restaurant Reservation System — Stage 1: Basic          ║
 * ╚══════════════════════════════════════════════════════════════════╝
 * <p>
 * Manages table reservations for a restaurant with <b>10 tables</b> of varying capacities.
 * Provides core functionality for reserving and canceling tables.
 * </p>
 *
 * <h2>📋 Features</h2>
 * <ul>
 *   <li><b>🔹 Reserve a Table</b> — By party size + time slot → returns table ID</li>
 *   <li><b>🔹 Cancel a Reservation</b> — By table ID + time slot</li>
 * </ul>
 *
 * <h2>📌 Business Rules</h2>
 * <ul>
 *   <li>✅ One reservation <b>per table</b> per time slot</li>
 *   <li>✅ Hourly slots only (e.g., 18:00, 19:00, 20:00)</li>
 *   <li>✅ Assigns the <b>smallest available</b> table that fits the party</li>
 * </ul>
 *
 * <h2>🪑 Table Capacities</h2>
 * <pre>
 *   Table 1:  4 seats
 *   Table 2:  6 seats
 *   Table 3:  8 seats
 *   Table 4:  12 seats
 *   Table 5:  16 seats
 *   Table 6:  20 seats
 *   Table 7:  26 seats
 *   Table 8:  30 seats
 *   Table 9:  36 seats
 *   Table 10: 40 seats
 * </pre>
 *
 * @author Metodi Velev
 * @version 1.0
 * @see Table
 * @see TimeSlot
 * @since 2026-08-03
 */
@Validated
@RestController("tables")
@RequestMapping("/tables")
public class RestaurantReservationController {

    private final RestaurantReservationService restaurantReservationService;

    public RestaurantReservationController(RestaurantReservationService restaurantReservationService) {
        this.restaurantReservationService = restaurantReservationService;
    }

    @PostMapping
    public ResponseEntity<Long> reserveTable(@Valid @RequestBody ReservationRequest reservationRequest) {
        return ResponseEntity.ok(
                restaurantReservationService.reserveTable(
                reservationRequest.partySize(),
                reservationRequest.timeSlotDto()
        ));
    }
}
