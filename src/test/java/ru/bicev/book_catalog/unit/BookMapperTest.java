package ru.bicev.book_catalog.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.bicev.book_catalog.dto.BookDto;
import ru.bicev.book_catalog.dto.BookRequest;
import ru.bicev.book_catalog.entity.Author;
import ru.bicev.book_catalog.entity.Book;
import ru.bicev.book_catalog.util.AuthorMapper;
import ru.bicev.book_catalog.util.BookMapper;
import ru.bicev.book_catalog.util.Genre;

public class BookMapperTest {

    private BookRequest request;
    private BookDto dto;
    private Book entity;
    private Author author;

    @BeforeEach
    public void setUp() {
        author = new Author(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"), "Firstname", "Lastname", 1000,
                "France");
        request = new BookRequest("1984", 1949, Genre.FANTASY, UUID.randomUUID());
        dto = new BookDto(UUID.fromString("321e4567-e89b-12d3-a456-426655440000"), "The Hobbit", 1937, Genre.FANTASY,
                AuthorMapper.toDto(author));
        entity = new Book(UUID.fromString("100e4567-e89b-12d3-a456-426655440000"), "Crime and Punishment", 1866,
                Genre.CLASSICS,
                author);

    }

    @Test
    void mapToEntityTest() {
        Book mapped = BookMapper.toEntity(dto, author);

        assertDtoEqualsBook(dto, mapped);

    }

    @Test
    void mapToEntityFromRequestTest() {
        Book mapped = BookMapper.toEntityFromRequest(request, author);

        assertNotNull(mapped.getId());
        assertBookEqualsRequest(request, mapped);

    }

    @Test
    void mapToDtoTest() {
        BookDto mapped = BookMapper.toDto(entity);

        assertBookEqualsDto(entity, mapped);
    }

    @Test
    void updateEntityTest() {

        BookRequest updateFrom = new BookRequest("1Q84", 0, Genre.ROMANCE, UUID.randomUUID());

        BookMapper.updateEntity(entity, updateFrom);

        assertEquals(updateFrom.title(), entity.getTitle());
        assertEquals(updateFrom.releaseYear(), entity.getReleaseYear());
        assertEquals(updateFrom.genre(), entity.getGenre());

    }

    @Test
    void nullThrowsNPE() {

        assertThrows(NullPointerException.class, () -> BookMapper.toDto(null));
        assertThrows(NullPointerException.class, () -> BookMapper.toEntity(null, null));
        assertThrows(NullPointerException.class, () -> BookMapper.toEntityFromRequest(null, null));
        assertThrows(NullPointerException.class, () -> BookMapper.updateEntity(null, null));

    }

    private void assertBookEqualsDto(Book book, BookDto dto) {
        assertEquals(book.getId(), dto.id());
        assertEquals(book.getTitle(), dto.title());
        assertEquals(book.getReleaseYear(), dto.releaseYear());
        assertEquals(book.getGenre(), dto.genre());
        assertEquals(book.getAuthor().getId(), dto.author().id());
        assertEquals(book.getAuthor().getFirstName(), dto.author().firstName());
        assertEquals(book.getAuthor().getLastName(), dto.author().lastName());
        assertEquals(book.getAuthor().getBirthYear(), dto.author().birthYear());
        assertEquals(book.getAuthor().getCountry(), dto.author().country());

    }

    private void assertBookEqualsRequest(BookRequest request, Book book) {
        assertEquals(request.title(), book.getTitle());
        assertEquals(request.releaseYear(), book.getReleaseYear());
        assertEquals(request.genre(), book.getGenre());
        assertEquals(author, book.getAuthor());

    }

    private void assertDtoEqualsBook(BookDto dto, Book book) {
        assertEquals(dto.id(), book.getId());
        assertEquals(dto.title(), book.getTitle());
        assertEquals(dto.releaseYear(), book.getReleaseYear());
        assertEquals(dto.genre(), book.getGenre());
        assertEquals(dto.author().id(), book.getAuthor().getId());
        assertEquals(dto.author().firstName(), book.getAuthor().getFirstName());
        assertEquals(dto.author().lastName(), book.getAuthor().getLastName());
        assertEquals(dto.author().birthYear(), book.getAuthor().getBirthYear());
        assertEquals(dto.author().country(), book.getAuthor().getCountry());
    }

}
