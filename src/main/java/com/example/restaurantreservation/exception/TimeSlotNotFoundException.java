package com.example.restaurantreservation.exception;

//@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class TimeSlotNotFoundException extends RuntimeException {
    public TimeSlotNotFoundException(String s) {
        super(s);
    }
}
