package ru.bicev.book_catalog.repo;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ru.bicev.book_catalog.entity.Book;
import ru.bicev.book_catalog.util.Genre;

public interface BookRepository extends JpaRepository<Book, UUID> {

    Page<Book> findByAuthorId(UUID authorId, Pageable pageable);

    Page<Book> findByReleaseYear(int releaseYear, Pageable pageable);

    Page<Book> findByReleaseYearBetween(int startYear, int endYear, Pageable pageable);

    Page<Book> findByGenre(Genre genre, Pageable pageable);

    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

}
