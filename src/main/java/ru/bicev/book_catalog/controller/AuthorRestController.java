package ru.bicev.book_catalog.controller;

import java.net.URI;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import ru.bicev.book_catalog.dto.AuthorDto;
import ru.bicev.book_catalog.dto.AuthorRequest;
import ru.bicev.book_catalog.service.AuthorService;

@RestController
@RequestMapping("/api/authors")
public class AuthorRestController {

    private final AuthorService authorService;
    private static final Logger logger = LoggerFactory.getLogger(AuthorRestController.class);

    public AuthorRestController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping
    public ResponseEntity<AuthorDto> createAuthor(@Valid @RequestBody AuthorRequest authorRequest) {
        logger.info("POST /api/authors lastName: {}", authorRequest.lastName());
        AuthorDto created = authorService.createAuthor(authorRequest);
        URI location = URI.create("/api/authors/" + created.id().toString());
        logger.info("Author created: {}", location);
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{authorId}")
    public ResponseEntity<AuthorDto> getAuthorById(@PathVariable UUID authorId) {
        logger.info("GET /api/authors authorId: {}", authorId);
        AuthorDto author = authorService.findAuthorById(authorId);
        return ResponseEntity.ok().body(author);
    }

    @PutMapping("/{authorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthorDto> updateAuthor(@PathVariable UUID authorId,
            @Valid @RequestBody AuthorRequest authorRequest) {
        logger.info("PUT /api/authors authorId: {}", authorId);
        AuthorDto updated = authorService.updateAuthor(authorId, authorRequest);
        return ResponseEntity.ok().body(updated);
    }

    @DeleteMapping("/{authorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAuthor(@PathVariable UUID authorId) {
        logger.info("DELETE /api/authors authorId: {}", authorId);
        authorService.deleteAuthorById(authorId);
        return ResponseEntity.noContent().build();
    }

}
