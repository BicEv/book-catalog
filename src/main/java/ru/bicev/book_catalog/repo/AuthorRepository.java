package ru.bicev.book_catalog.repo;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import ru.bicev.book_catalog.entity.Author;

public interface AuthorRepository extends CrudRepository<Author, UUID> {

}
