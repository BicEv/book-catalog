package ru.bicev.book_catalog.dto;

import java.time.LocalDateTime;

public record ValidationErrorDto(String field, String message, String errorCode, LocalDateTime timestamp) {

}
