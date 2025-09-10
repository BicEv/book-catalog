package ru.bicev.book_catalog.dto;

import java.time.LocalDateTime;

public record ErrorDto(String message, String errorCode, int statusCode, LocalDateTime timestamp) {

}
