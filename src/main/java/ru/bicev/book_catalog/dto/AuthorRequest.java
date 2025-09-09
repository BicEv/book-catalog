package ru.bicev.book_catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import ru.bicev.book_catalog.util.MaxBirthYear;

public record AuthorRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Min(-2600) @MaxBirthYear int birthYear,
        @NotBlank String country) {

}
