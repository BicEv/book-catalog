package ru.bicev.book_catalog.controller;

import java.net.URI;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import ru.bicev.book_catalog.dto.BookDto;
import ru.bicev.book_catalog.dto.BookRequest;
import ru.bicev.book_catalog.dto.ErrorDto;
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

        @Operation(summary = "Create book", security = @SecurityRequirement(name = "bearerAuth"), description = "Create new book and return its BookDto")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Book was created", content = @Content(schema = @Schema(implementation = BookDto.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
                        @ApiResponse(responseCode = "409", description = "Book with such combination of parameters already exists", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
        })
        @PostMapping
        public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookRequest bookRequest) {
                logger.info("POST /api/books title: {}, authorId: {}", bookRequest.title(), bookRequest.authorId());
                BookDto created = bookService.createBook(bookRequest);
                URI location = URI.create("/api/books/" + created.id().toString());
                logger.info("Created URI: {}", location.toString());
                return ResponseEntity.created(location).body(created);
        }

        @Operation(summary = "Get book by id", description = "Find book by id and return its BookDto")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Book was found", content = @Content(schema = @Schema(implementation = BookDto.class))),
                        @ApiResponse(responseCode = "404", description = "Book was not found", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
        })
        @GetMapping("/{bookId}")
        public ResponseEntity<BookDto> getBookById(@PathVariable UUID bookId) {
                logger.info("GET /api/books bookId: {}", bookId);
                BookDto book = bookService.findBookById(bookId);
                return ResponseEntity.ok().body(book);
        }

        @Operation(summary = "Update book", security = @SecurityRequirement(name = "bearerAuth"), description = "Update book and return updated BookDto")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Book was updated", content = @Content(schema = @Schema(implementation = BookDto.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
                        @ApiResponse(responseCode = "403", description = "Current user is not an admin", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
                        @ApiResponse(responseCode = "404", description = "Book was not found", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
        })
        @PutMapping("/{bookId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<BookDto> updateBook(@PathVariable UUID bookId,
                        @Valid @RequestBody BookRequest bookRequest) {
                logger.info("PUT /api/books bookId: {}", bookId);
                BookDto updated = bookService.updateBook(bookId, bookRequest);
                return ResponseEntity.ok().body(updated);
        }

        @Operation(summary = "Delete book", security = @SecurityRequirement(name = "bearerAuth"), description = "Delete book")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Book was deleted"),
                        @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
                        @ApiResponse(responseCode = "403", description = "Current user is not an admin", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
                        @ApiResponse(responseCode = "404", description = "Book was not found", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
        })
        @DeleteMapping("/{bookId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> deleteBook(@PathVariable UUID bookId) {
                logger.info("DELETE /api/books bookId: {}", bookId);
                bookService.deleteBook(bookId);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Get books by parameters", description = "Find books by parameters and return PagedResponse")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Books was found", content = @Content(schema = @Schema(implementation = BookDto.class))),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
        })

        @GetMapping
        public ResponseEntity<PagedResponse<BookDto>> getBooks(
                        @RequestParam(required = false) UUID authorId,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) Integer releaseYear,
                        @RequestParam(required = false) Integer startYear,
                        @RequestParam(required = false) Integer endYear,
                        @RequestParam(required = false) Genre genre,
                        @RequestParam(required = false) String title,
                        @ParameterObject @PageableDefault(page = 0, size = 10, sort = "title") Pageable pageable) {
                logger.info(
                                "GET /api/books authorId: {}, name: {}, releaseYear: {}, startYear: {}, endYear: {}, genre: {}, title: {}",
                                authorId, name, releaseYear, startYear, endYear, genre, title);
                PagedResponse<BookDto> response = bookService.findBooks(authorId, name, releaseYear, startYear, endYear,
                                genre,
                                title,
                                pageable);

                return ResponseEntity.ok(response);
        }

}
