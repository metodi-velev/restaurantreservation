package com.example.restaurantreservation.controller;

import com.example.restaurantreservation.dto.ErrorDto;
import com.example.restaurantreservation.dto.ReservationDto;
import com.example.restaurantreservation.dto.ReservationRequest;
import com.example.restaurantreservation.dto.ReservationResponseWithPictureWithEndpoint;
import com.example.restaurantreservation.entity.Table;
import com.example.restaurantreservation.entity.TimeSlot;
import com.example.restaurantreservation.service.RestaurantReservationService;
import com.example.restaurantreservation.service.RestaurantReservationServiceWithPicture;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@Tag(name = "Table Reservations", description = "Endpoints for managing restaurant table reservations")
@Validated
@RestController("tables")
@RequestMapping("/tables")
public class RestaurantReservationController {

    private final RestaurantReservationService restaurantReservationService;
    private final RestaurantReservationServiceWithPicture restaurantReservationServiceWithPicture;

    public RestaurantReservationController(RestaurantReservationService restaurantReservationService,
                                           RestaurantReservationServiceWithPicture restaurantReservationServiceWithPicture) {
        this.restaurantReservationService = restaurantReservationService;
        this.restaurantReservationServiceWithPicture = restaurantReservationServiceWithPicture;
    }

    @Operation(
            summary = "Reserve a table",
            description = "Creates a new reservation by finding the smallest available table that fits the party size for the given time slot."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Table successfully reserved",
                    content = @Content(schema = @Schema(implementation = Long.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters or business rule violation",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No suitable table available for the given party size and time slot",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))
            )
    })
    @PostMapping
    public ResponseEntity<Long> reserveTable(@Valid @RequestBody ReservationRequest reservationRequest) {
        return ResponseEntity.ok(
                restaurantReservationService.reserveTable(
                reservationRequest.partySize(),
                reservationRequest.timeSlotDto()
        ));
    }

    @PostMapping("/with-picture")
    public ResponseEntity<ReservationResponseWithPictureWithEndpoint> reserveTableWithPicture(@Valid @RequestBody ReservationRequest reservationRequest) {

        ReservationResponseWithPictureWithEndpoint resp = restaurantReservationServiceWithPicture.reserveTableWithPicture(
                reservationRequest.partySize(),
                reservationRequest.timeSlotDto()
        );

        return ResponseEntity.ok()
                .body(resp);
    }

    @DeleteMapping("{tableId}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable("tableId") Long tableId,
            @Valid @RequestBody ReservationRequest reservationRequest) {
        restaurantReservationService.cancelReservation(
                tableId,
                reservationRequest.timeSlotDto()
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
        return ResponseEntity.ok(
                restaurantReservationService.getAllReservations()
        );
    }

    @GetMapping("/reservations/{tableId}")
    public ResponseEntity<List<ReservationDto>> getReservationForTableId(@PathVariable("tableId") Long tableId) {
        return ResponseEntity.ok(
                restaurantReservationService.getReservationForTableId(tableId)
        );
    }
}
