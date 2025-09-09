package ru.bicev.book_catalog.util;

import java.time.Year;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxBirthValidator implements ConstraintValidator<MaxBirthYear, Integer> {
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null)
            return true;
        int maxYear = Year.now().getValue() - 10;
        return value <= maxYear;
    }

}
