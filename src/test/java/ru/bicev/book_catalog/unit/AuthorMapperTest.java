package ru.bicev.book_catalog.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.bicev.book_catalog.dto.AuthorDto;
import ru.bicev.book_catalog.dto.AuthorRequest;
import ru.bicev.book_catalog.entity.Author;
import ru.bicev.book_catalog.util.AuthorMapper;

public class AuthorMapperTest {

    private Author author;
    private AuthorDto authorDto;
    private AuthorRequest authorRequest;

    @BeforeEach
    public void setUp() {
        author = new Author(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"), "John", "Tolkien", 1892, "UK");
        authorDto = new AuthorDto(UUID.fromString("123e4567-e89b-12d3-a456-426655440000"), "Leo", "Tolstoy", 1828,
                "Russia");
        authorRequest = new AuthorRequest("Cormac", "McCarthy", 1933, "USA");
    }

    @Test
    void mapToEntityTest() {
        Author mapped = AuthorMapper.toEntity(authorDto);

        assertDtoEqualsAuthor(authorDto, mapped);

    }

    @Test
    void mapToEntityFromRequestTest() {
        Author mapped = AuthorMapper.toEntityFromRequest(authorRequest);

        assertNotNull(mapped.getId());
        assertAuthorEqualsRequest(authorRequest, mapped);

    }

    @Test
    void mapToDtoTest() {
        AuthorDto mapped = AuthorMapper.toDto(author);

        assertAuthorEqualsDto(author, mapped);
    }

    @Test
    void updateEntityTest() {
        Author toUpdate = new Author(UUID.randomUUID(), "Haruki", "Murakami", 0, "Japan");
        AuthorRequest updateFrom = new AuthorRequest("FirstName", "LastName", 1984, "SAR");

        AuthorMapper.updateEntity(toUpdate, updateFrom);

        assertEquals(updateFrom.firstName(), toUpdate.getFirstName());
        assertEquals(updateFrom.lastName(), toUpdate.getLastName());
        assertEquals(updateFrom.birthYear(), toUpdate.getBirthYear());
        assertEquals(updateFrom.country(), toUpdate.getCountry());
    }

    @Test
    void nullThrowsNPE() {

        assertThrows(NullPointerException.class, () -> AuthorMapper.toDto(null));
        assertThrows(NullPointerException.class, () -> AuthorMapper.toEntity(null));
        assertThrows(NullPointerException.class, () -> AuthorMapper.toEntityFromRequest(null));
        assertThrows(NullPointerException.class, () -> AuthorMapper.updateEntity(null, null));

    }

    private void assertAuthorEqualsDto(Author author, AuthorDto dto) {
        assertEquals(author.getId(), dto.id());
        assertEquals(author.getFirstName(), dto.firstName());
        assertEquals(author.getLastName(), dto.lastName());
        assertEquals(author.getBirthYear(), dto.birthYear());
        assertEquals(author.getCountry(), dto.country());
    }

    private void assertAuthorEqualsRequest(AuthorRequest request, Author author) {
        assertEquals(request.firstName(), author.getFirstName());
        assertEquals(request.lastName(), author.getLastName());
        assertEquals(request.birthYear(), author.getBirthYear());
        assertEquals(request.country(), author.getCountry());
    }

    private void assertDtoEqualsAuthor(AuthorDto dto, Author author) {
        assertEquals(dto.id(), author.getId());
        assertEquals(dto.firstName(), author.getFirstName());
        assertEquals(dto.lastName(), author.getLastName());
        assertEquals(dto.birthYear(), author.getBirthYear());
        assertEquals(dto.country(), author.getCountry());
    }

}
