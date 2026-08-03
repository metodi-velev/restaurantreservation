package com.example.restaurantreservation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class TimeFormatValidator implements ConstraintValidator<ValidTimeFormat, LocalTime> {

    private static final Pattern PATTERN = Pattern.compile("^([01]\\d|2[0-3]):00$");

    @Override
    public boolean isValid(LocalTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        LocalTime time = LocalTime.parse(value.toString(), DateTimeFormatter.ofPattern("HH:mm"));
        if (!PATTERN.matcher(value.toString()).matches() || time.getMinute() != 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Time must be in HH:00 format with round hours (e.g., 18:00, 19:00, 20:00). Invalid value: "
                            + value + "."
            ).addConstraintViolation();
            return false;
        }
        return true;
    }
}
