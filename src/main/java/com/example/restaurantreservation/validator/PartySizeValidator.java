package com.example.restaurantreservation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PartySizeValidator implements ConstraintValidator<ValidPartySize, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        if (value < 4) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Party size value must be at least 4. Invalid value: "
                            + value + "."
            ).addConstraintViolation();
            return false;
        }
        return true;
    }
}
