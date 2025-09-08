package ru.bicev.book_catalog.dto;

public record AuthorRequest(String firstName, String lastName, int birthYear, String country) {

}
