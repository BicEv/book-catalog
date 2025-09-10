package ru.bicev.book_catalog.controller;

import java.net.URI;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import ru.bicev.book_catalog.dto.BookDto;
import ru.bicev.book_catalog.dto.BookRequest;
import ru.bicev.book_catalog.dto.PagedResponse;
import ru.bicev.book_catalog.service.BookService;
import ru.bicev.book_catalog.util.Genre;

@RestController
@RequestMapping("/api/books")
public class BookRestController {

    private final BookService bookService;
    private static final Logger logger = LoggerFactory.getLogger(BookRestController.class);

    public BookRestController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookRequest bookRequest) {
        logger.info("POST /api/books title: {}, authorId: {}", bookRequest.title(), bookRequest.authorId());
        BookDto created = bookService.createBook(bookRequest);
        URI location = URI.create("/api/books/" + created.id().toString());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDto> getBookById(@PathVariable UUID bookId) {
        logger.info("GET /api/books bookId: {}", bookId);
        BookDto book = bookService.findBookById(bookId);
        return ResponseEntity.ok().body(book);
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<BookDto> updateBook(@PathVariable UUID bookId, @Valid @RequestBody BookRequest bookRequest) {
        logger.info("PUT /api/books bookId: {}", bookId);
        BookDto updated = bookService.updateBook(bookId, bookRequest);
        return ResponseEntity.ok().body(updated);
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID bookId) {
        logger.info("DELETE /api/books bookId: {}", bookId);
        bookService.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedResponse<BookDto>> getBooks(
            @RequestParam(required = false) UUID authorId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer releaseYear,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(required = false) Genre genre,
            @RequestParam(required = false) String title,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        logger.info(
                "GET /api/books authorId: {}, name: {}, releaseYear: {}, startYear: {}, endYear: {}, genre: {}, title: {}",
                authorId, name, releaseYear, startYear, endYear, genre, title);
        PagedResponse<BookDto> response = bookService.findBooks(authorId, name, releaseYear, startYear, endYear, genre,
                title,
                pageable);

        return ResponseEntity.ok(response);
    }

}
