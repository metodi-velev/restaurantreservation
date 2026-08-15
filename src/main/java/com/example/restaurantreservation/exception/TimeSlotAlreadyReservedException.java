package com.example.restaurantreservation.exception;

//@ResponseStatus(HttpStatus.CONFLICT)
public class TimeSlotAlreadyReservedException extends RuntimeException {
    public TimeSlotAlreadyReservedException(String s) {
        super(s);
    }
}
