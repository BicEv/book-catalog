package ru.bicev.book_catalog.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.bicev.book_catalog.entity.Author;

public interface AuthorRepository extends JpaRepository<Author, UUID> {

}
