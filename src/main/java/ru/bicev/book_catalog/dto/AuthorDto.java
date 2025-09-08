package ru.bicev.book_catalog.dto;

import java.util.UUID;

public record AuthorDto(UUID id, String firstName, String lastName, int birthYear, String country) {

}
