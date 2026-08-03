package com.example.restaurantreservation;

import org.springframework.boot.SpringApplication;

public class TestRestaurantReservationApplication {

	public static void main(String[] args) {
		SpringApplication.from(RestaurantReservationApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
