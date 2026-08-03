package com.example.restaurantreservation.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeFormatValidator.class)
public @interface ValidTimeFormat {
    String message() default "Time must be in HH:mm format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
