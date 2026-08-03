package com.example.restaurantreservation.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PartySizeValidator.class)
public @interface ValidPartySize {
    String message() default "Party size value must be at least 4.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
