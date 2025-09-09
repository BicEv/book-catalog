package ru.bicev.book_catalog.util;

import java.time.Year;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxReleaseValidator implements ConstraintValidator<MaxReleaseYear, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value <= Year.now().getValue();
    }

}
