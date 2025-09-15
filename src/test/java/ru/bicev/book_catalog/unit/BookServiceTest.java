package ru.bicev.book_catalog.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ru.bicev.book_catalog.dto.BookDto;
import ru.bicev.book_catalog.dto.BookRequest;
import ru.bicev.book_catalog.dto.PagedResponse;
import ru.bicev.book_catalog.entity.Author;
import ru.bicev.book_catalog.entity.Book;
import ru.bicev.book_catalog.exception.AuthorNotFoundException;
import ru.bicev.book_catalog.exception.BookNotFoundException;
import ru.bicev.book_catalog.repo.AuthorRepository;
import ru.bicev.book_catalog.repo.BookRepository;
import ru.bicev.book_catalog.service.BookService;
import ru.bicev.book_catalog.util.Genre;

public class BookServiceTest {

    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private BookRequest request;
    private Book entity1;
    private Book entity2;
    private Book entity3;
    private Author author1;
    private Author author2;
    private UUID authId1;
    private UUID authId2;
    private UUID bookId1;
    private UUID bookId2;
    private UUID bookId3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authId1 = UUID.fromString("123e4567-e89b-12d3-a456-426655440000");
        authId2 = UUID.fromString("124e4567-e89b-12d3-a456-426655440000");
        bookId1 = UUID.fromString("100e4567-e89b-12d3-a456-426655440000");
        bookId2 = UUID.fromString("200e4567-e89b-12d3-a456-426655440000");
        bookId3 = UUID.fromString("300e4567-e89b-12d3-a456-426655440000");
        request = new BookRequest("War and Peace", 1890, Genre.CLASSICS,
                authId1);
        author1 = new Author(authId1, "Leo", "Tolstoy", 1833, "Russia");
        author2 = new Author(authId2, "John", "Tolkien", 1880, "UK");
        entity1 = new Book(bookId1, "War and Peace", 1890,
                Genre.CLASSICS, author1);
        entity2 = new Book(bookId2, "Anna Karenina", 1895,
                Genre.ROMANCE, author1);
        entity3 = new Book(bookId3, "LOTR: Fellowship of the Ring",
                1910,
                Genre.FANTASY, author2);

    }

    @Test
    void createBookSuccess() {
        when(authorRepository.findById(authId1))
                .thenReturn(Optional.of(author1));
        when(bookRepository.save(any())).thenReturn(entity1);

        BookDto created = bookService.createBook(request);

        assertEquals(entity1.getId(), created.id());
        assertEquals(entity1.getTitle(), created.title());
        assertEquals(entity1.getAuthor().getId(), created.author().id());

        verify(authorRepository, times(1)).findById(authId1);
        verify(bookRepository, times(1)).save(any());
    }

    @Test
    void createBookThrowAuthorNotFound() {
        when(authorRepository.findById(authId1))
                .thenReturn(Optional.empty());

        assertThrows(AuthorNotFoundException.class, () -> bookService.createBook(request));
    }

    @Test
    void getBookByIdSuccess() {
        when(bookRepository.findById(bookId1))
                .thenReturn(Optional.of(entity1));

        BookDto found = bookService.findBookById(bookId1);

        assertEquals(entity1.getId(), found.id());
        assertEquals(entity1.getTitle(), found.title());
        assertEquals(entity1.getGenre(), found.genre());
        assertEquals(entity1.getAuthor().getId(), found.author().id());

        verify(bookRepository, times(1)).findById(bookId1);

    }

    @Test
    void getBookByIdThrowsNotFound() {
        when(bookRepository.findById(bookId3)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.findBookById(bookId3));
    }

    @Test
    void updateBookSuccess() {
        when(bookRepository.findById(bookId1)).thenReturn(Optional.of(entity1));
        when(authorRepository.findById(authId1)).thenReturn(Optional.of(author1));

        BookDto updated = bookService.updateBook(bookId1, request);
        assertEquals(entity1.getId(), updated.id());
        assertEquals(entity1.getTitle(), updated.title());
        assertEquals(entity1.getAuthor().getId(), updated.author().id());

        verify(bookRepository, times(1)).save(any());
    }

    @Test
    void updateBookThrowsNotFound() {
        when(bookRepository.findById(bookId1)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.updateBook(bookId1, request));
    }

    @Test
    void deleteBookSuccess() {
        when(bookRepository.findById(bookId1)).thenReturn(Optional.of(entity1));

        bookService.deleteBook(bookId1);

        verify(bookRepository, times(1)).delete(entity1);
    }

    @Test
    void deleteBookThrowsNotFound() {
        when(bookRepository.findById(bookId1)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.deleteBook(bookId1));
    }

    @Test
    void findAllBooksSuccess() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(entity1, entity2, entity3));

        when(bookRepository.findAll(pageable)).thenReturn(page);

        PagedResponse<BookDto> result = bookService.findBooks(null, null, null, null, null, null, null, pageable);

        assertEquals(3, result.content().size());
        assertEquals(entity1.getTitle(), result.content().get(0).title());
        assertEquals(entity2.getTitle(), result.content().get(1).title());
        assertEquals(entity3.getTitle(), result.content().get(2).title());
        assertTrue(result.isFirst());
        assertTrue(result.last());
        assertEquals(1, result.totalPages());

        verify(bookRepository, times(1)).findAll(pageable);
    }

    @Test
    void findBooksByAuthorIdSuccess() {
        // Other paramethrized findBy* methods are same, only paramethers change
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(entity1, entity2));

        when(bookRepository.findByAuthorId(authId1, pageable)).thenReturn(page);

        PagedResponse<BookDto> result = bookService.findBooks(authId1, null, null, null, null, null, null, pageable);

        assertEquals(2, result.content().size());
        assertEquals(entity1.getTitle(), result.content().get(0).title());
        assertEquals(entity2.getTitle(), result.content().get(1).title());
        assertTrue(result.isFirst());
        assertTrue(result.last());
        assertEquals(1, result.totalPages());

        verify(bookRepository, times(1)).findByAuthorId(authId1, pageable);
    }

}
