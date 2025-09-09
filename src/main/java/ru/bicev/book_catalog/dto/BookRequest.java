package ru.bicev.book_catalog.dto;

import java.util.UUID;

import ru.bicev.book_catalog.util.Genre;

public record BookRequest(String title, int releaseYear, Genre genre, UUID authorId) {

}
