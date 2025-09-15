package ru.bicev.book_catalog.dto;

import java.util.UUID;

import ru.bicev.book_catalog.util.Genre;

public record BookDto(UUID id, String title, int releaseYear, Genre genre, AuthorDto author) {

}
