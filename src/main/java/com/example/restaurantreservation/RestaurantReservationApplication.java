package com.example.restaurantreservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class RestaurantReservationApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestaurantReservationApplication.class, args);
	}

/*	Stage 1: Basic Reservation

	Create a restaurant reservation system.

	The restaurant has 10 tables with different capacities.
	Functionality:
			1)Add an ability to reserve a table by providing party size and time slot
			     (hourly slots only, e.g., 18:00, 19:00, 20:00). Returns the reserved table ID.
 			2)Add an ability to cancel a reservation by providing table ID and time slot.
	Each table can only have one reservation per time slot. Always assign the smallest available table that fits the party.
*/

}
