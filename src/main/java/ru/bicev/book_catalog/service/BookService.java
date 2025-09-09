package ru.bicev.book_catalog.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.bicev.book_catalog.dto.BookDto;
import ru.bicev.book_catalog.dto.BookRequest;
import ru.bicev.book_catalog.entity.Author;
import ru.bicev.book_catalog.entity.Book;
import ru.bicev.book_catalog.exception.AuthorNotFoundException;
import ru.bicev.book_catalog.exception.BookNotFoundException;
import ru.bicev.book_catalog.repo.AuthorRepository;
import ru.bicev.book_catalog.repo.BookRepository;
import ru.bicev.book_catalog.util.BookMapper;
import ru.bicev.book_catalog.util.Genre;

@Service
public class BookService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public BookService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    private Author extractAuthor(UUID authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new AuthorNotFoundException(String.format("Author not found: %s", authorId)));
    }

    @Transactional
    public BookDto createBook(BookRequest bookRequest) {
        Author author = extractAuthor(bookRequest.authorId());
        Book book = BookMapper.toEntityFromRequest(bookRequest, author);
        Book savedBook = bookRepository.save(book);
        return BookMapper.toDto(savedBook);
    }

    public BookDto findBookById(UUID bookId) {
        Book foundBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(String.format("Book not found: %s", bookId)));
        return BookMapper.toDto(foundBook);
    }

    @Transactional
    public BookDto updateBook(UUID bookId, BookRequest bookRequest) {
        Book foundBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(String.format("Book not found: %s", bookId)));
        Author newAuthor = extractAuthor(bookRequest.authorId());
        BookMapper.updateEntity(foundBook, bookRequest);
        foundBook.setAuthor(newAuthor);
        bookRepository.save(foundBook);
        return BookMapper.toDto(foundBook);
    }

    @Transactional
    public void deleteBook(UUID bookId) {
        Book foundBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(String.format("Book not found: %s", bookId)));
        bookRepository.delete(foundBook);
    }

    public Page<BookDto> findByAuthorId(UUID authorId, Pageable pageable) {
        return bookRepository.findByAuthorId(authorId, pageable).map(BookMapper::toDto);
    }

    public Page<BookDto> findByAuthorName(String name, Pageable pageable) {
        return bookRepository.findByAuthorName(name, pageable).map(BookMapper::toDto);
    }

    public Page<BookDto> findByReleaseYear(int releaseYear, Pageable pageable) {
        return bookRepository.findByReleaseYear(releaseYear, pageable).map(BookMapper::toDto);
    }

    public Page<BookDto> findByReleaseYearBetween(int startYear, int endYear, Pageable pageable) {
        return bookRepository.findByReleaseYearBetween(startYear, endYear, pageable).map(BookMapper::toDto);
    }

    public Page<BookDto> findByGenre(Genre genre, Pageable pageable) {
        return bookRepository.findByGenre(genre, pageable).map(BookMapper::toDto);
    }

    public Page<BookDto> findByTitleContaining(String title, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(title, pageable).map(BookMapper::toDto);
    }

}
