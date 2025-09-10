package ru.bicev.book_catalog.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ru.bicev.book_catalog.dto.ErrorDto;
import ru.bicev.book_catalog.dto.ValidationErrorDto;
import ru.bicev.book_catalog.exception.AuthorNotFoundException;
import ru.bicev.book_catalog.exception.BookNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthorNotFoundException.class)
    public ResponseEntity<ErrorDto> handleAuthorNotFoundException(AuthorNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorDto error = extractError(ex, "AUTHOR_NOT_FOUND", status);

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorDto> handleBookNotFoundException(BookNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorDto error = extractError(ex, "BOOK_NOT_FOUND", status);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ValidationErrorDto>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ValidationErrorDto> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new ValidationErrorDto(
                        error.getField(),
                        error.getDefaultMessage(),
                        "VALIDATION_ERROR",
                        LocalDateTime.now()

                )).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDto> handleRuntimeException(RuntimeException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorDto error = extractError(ex, "INTERNAL_SERVER_ERROR", status);
        return ResponseEntity.status(500).body(error);
    }

    private ErrorDto extractError(RuntimeException ex, String errorCode, HttpStatus status) {
        return new ErrorDto(ex.getMessage(), errorCode, status.value(), LocalDateTime.now());
    }

}
