package ru.bicev.book_catalog.util;

import java.util.UUID;

import ru.bicev.book_catalog.dto.BookDto;
import ru.bicev.book_catalog.dto.BookRequest;
import ru.bicev.book_catalog.entity.Author;
import ru.bicev.book_catalog.entity.Book;

public class BookMapper {

    public static BookDto toDto(Book book) {
        BookDto bookDto = new BookDto(
                book.getId(),
                book.getTitle(),
                book.getReleaseYear(),
                book.getGenre(),
                AuthorMapper.toDto(book.getAuthor()));
        return bookDto;
    }

    public static Book toEntity(BookDto bookDto, Author author) {
        return Book.builder()
                .id(bookDto.id())
                .title(bookDto.title())
                .releaseYear(bookDto.releaseYear())
                .genre(bookDto.genre())
                .author(author)
                .build();

    }

    public static Book toEntityFromRequest(BookRequest bookRequest, Author author) {
        return Book.builder()
                .id(UUID.randomUUID())
                .title(bookRequest.title())
                .releaseYear(bookRequest.releaseYear())
                .genre(bookRequest.genre())
                .author(author)
                .build();
    }

    public static void updateEntity(Book book, BookRequest bookRequest) {
        book.setTitle(bookRequest.title());
        book.setReleaseYear(bookRequest.releaseYear());
        book.setGenre(bookRequest.genre());
    }

}
