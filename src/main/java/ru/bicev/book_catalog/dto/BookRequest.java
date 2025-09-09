package ru.bicev.book_catalog.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.bicev.book_catalog.util.Genre;
import ru.bicev.book_catalog.util.MaxReleaseYear;

public record BookRequest(
        @NotBlank String title,
        @Min(0) @MaxReleaseYear int releaseYear,
        @NotNull Genre genre,
        @NotNull UUID authorId) {

}
