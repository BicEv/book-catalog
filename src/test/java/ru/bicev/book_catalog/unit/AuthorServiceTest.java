package ru.bicev.book_catalog.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ru.bicev.book_catalog.dto.AuthorDto;
import ru.bicev.book_catalog.dto.AuthorRequest;
import ru.bicev.book_catalog.entity.Author;
import ru.bicev.book_catalog.exception.AuthorNotFoundException;
import ru.bicev.book_catalog.repo.AuthorRepository;
import ru.bicev.book_catalog.service.AuthorService;

public class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    private AuthorRequest request;
    private AuthorRequest updateReq;
    private Author entity;
    private Author updated;
    private UUID authorId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authorId = UUID.fromString("123e4567-e89b-12d3-a456-426655440000");
        request = new AuthorRequest("Leo", "Tolstoy", 1828, "Russia");
        updateReq = new AuthorRequest("Updated name", "Updatedtolstoy", 1830, "Russian Empire");
        entity = new Author(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"), "Leo", "Tolstoy", 1828, "Russia");
        updated = new Author(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"), "Updated name", "Updatedtolstoy",
                1830, "Russian Empire");
    }

    @Test
    void testCreateAuthorSuccess() {
        when(authorRepository.save(any())).thenReturn(entity);

        AuthorDto saved = authorService.createAuthor(request);

        assertEquals(entity.getId(), saved.id());
        assertEquals(entity.getFirstName(), saved.firstName());
        assertEquals(entity.getLastName(), saved.lastName());
        assertEquals(entity.getBirthYear(), saved.birthYear());
        assertEquals(entity.getCountry(), saved.country());

        verify(authorRepository, times(1)).save(any());
    }

    @Test
    void getAuthorByIdSuccess() {
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(entity));

        AuthorDto found = authorService.findAuthorById(authorId);

        assertEquals(entity.getId(), found.id());
        assertEquals(entity.getFirstName(), found.firstName());
        assertEquals(entity.getLastName(), found.lastName());
        assertEquals(entity.getBirthYear(), found.birthYear());
        assertEquals(entity.getCountry(), found.country());

        verify(authorRepository, times(1)).findById(authorId);
    }

    @Test
    void getAuthorByIdThrowsNotFound() {
        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        assertThrows(AuthorNotFoundException.class, () -> authorService.findAuthorById(authorId));
    }

    @Test
    void updateAuthorSuccess() {
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(entity));
        when(authorRepository.save(any())).thenReturn(updated);

        AuthorDto updatedEntity = authorService.updateAuthor(authorId, updateReq);

        assertNotNull(updatedEntity.id());
        assertEquals(updateReq.firstName(), updatedEntity.firstName());
        assertEquals(updateReq.lastName(), updatedEntity.lastName());
        assertEquals(updateReq.birthYear(), updatedEntity.birthYear());
        assertEquals(updateReq.country(), updatedEntity.country());

        verify(authorRepository, times(1)).save(any());
    }

    @Test
    void updateAuthorThrowsNotFoundException() {
        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        assertThrows(AuthorNotFoundException.class, () -> authorService.updateAuthor(authorId, request));
    }

    @Test
    void deleteAuthorSuccess() {
        when(authorRepository.existsById(authorId)).thenReturn(true);

        authorService.deleteAuthorById(authorId);

        verify(authorRepository, times(1)).deleteById(authorId);
    }

    @Test
    void deleteAuthorThrowsNotFound() {
        when(authorRepository.existsById(authorId)).thenReturn(false);

        assertThrows(AuthorNotFoundException.class, () -> authorService.deleteAuthorById(authorId));
    }

}
