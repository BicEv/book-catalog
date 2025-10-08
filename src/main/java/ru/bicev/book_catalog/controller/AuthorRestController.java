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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import ru.bicev.book_catalog.dto.AuthorDto;
import ru.bicev.book_catalog.dto.AuthorRequest;
import ru.bicev.book_catalog.dto.ErrorDto;
import ru.bicev.book_catalog.service.AuthorService;

@RestController
@RequestMapping("/api/authors")
public class AuthorRestController {

    private final AuthorService authorService;
    private static final Logger logger = LoggerFactory.getLogger(AuthorRestController.class);

    public AuthorRestController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @Operation(summary = "Create author", security = @SecurityRequirement(name = "bearerAuth"), description = "Create new author and return his AuthorDto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Author created", content = @Content(schema = @Schema(implementation = AuthorDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "409", description = "Author with such combination of parameters already exists", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    @PostMapping
    public ResponseEntity<AuthorDto> createAuthor(@Valid @RequestBody AuthorRequest authorRequest) {
        logger.info("POST /api/authors lastName: {}", authorRequest.lastName());
        AuthorDto created = authorService.createAuthor(authorRequest);
        URI location = URI.create("/api/authors/" + created.id().toString());
        logger.info("Author created: {}", location);
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Get author by Id", description = "Find author by id return AuthorDto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author found", content = @Content(schema = @Schema(implementation = AuthorDto.class))),
            @ApiResponse(responseCode = "404", description = "Author was not found", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    @GetMapping("/{authorId}")
    public ResponseEntity<AuthorDto> getAuthorById(@PathVariable UUID authorId) {
        logger.info("GET /api/authors authorId: {}", authorId);
        AuthorDto author = authorService.findAuthorById(authorId);
        return ResponseEntity.ok().body(author);
    }

    @Operation(summary = "Update author", security = @SecurityRequirement(name = "bearerAuth"), description = "Update existing author and return updated AuthorDto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author updated", content = @Content(schema = @Schema(implementation = AuthorDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Current user is not Admin", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Author was not found", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    @PutMapping("/{authorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthorDto> updateAuthor(@PathVariable UUID authorId,
            @Valid @RequestBody AuthorRequest authorRequest) {
        logger.info("PUT /api/authors authorId: {}", authorId);
        AuthorDto updated = authorService.updateAuthor(authorId, authorRequest);
        return ResponseEntity.ok().body(updated);
    }

    @Operation(summary = "Delete author", security = @SecurityRequirement(name = "bearerAuth"), description = "Delete existing author")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author updated", content = @Content(schema = @Schema(implementation = AuthorDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Current user is not Admin", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "Author was not found", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    @DeleteMapping("/{authorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAuthor(@PathVariable UUID authorId) {
        logger.info("DELETE /api/authors authorId: {}", authorId);
        authorService.deleteAuthorById(authorId);
        return ResponseEntity.noContent().build();
    }

}
