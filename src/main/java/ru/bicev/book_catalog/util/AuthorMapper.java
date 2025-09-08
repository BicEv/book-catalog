package ru.bicev.book_catalog.util;

import java.util.UUID;

import ru.bicev.book_catalog.dto.AuthorDto;
import ru.bicev.book_catalog.dto.AuthorRequest;
import ru.bicev.book_catalog.entity.Author;

public class AuthorMapper {

    public static Author toEntity(AuthorDto authorDto) {
        return Author.builder()
                .id(authorDto.id())
                .firstName(authorDto.firstName())
                .lastName(authorDto.lastName())
                .birthYear(authorDto.birthYear())
                .country(authorDto.country())
                .build();

    }

    public static Author toEntityFromRequest(AuthorRequest authorRequest) {
        return Author.builder()
                .id(UUID.randomUUID())
                .firstName(authorRequest.firstName())
                .lastName(authorRequest.lastName())
                .birthYear(authorRequest.birthYear())
                .country(authorRequest.country())
                .build();
    }

    public static AuthorDto toDto(Author author) {
        return new AuthorDto(author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getBirthYear(),
                author.getCountry());
    }

    public static void updateEntity(Author author, AuthorRequest authorRequest) {
        author.setFirstName(authorRequest.firstName());
        author.setLastName(authorRequest.lastName());
        author.setBirthYear(authorRequest.birthYear());
        author.setCountry(authorRequest.country());
    }

}
