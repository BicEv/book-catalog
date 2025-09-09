package ru.bicev.book_catalog.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.bicev.book_catalog.dto.AuthorDto;
import ru.bicev.book_catalog.dto.AuthorRequest;
import ru.bicev.book_catalog.entity.Author;
import ru.bicev.book_catalog.exception.AuthorNotFoundException;
import ru.bicev.book_catalog.repo.AuthorRepository;
import ru.bicev.book_catalog.util.AuthorMapper;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Transactional
    public AuthorDto createAuthor(AuthorRequest authorRequest) {
        Author author = AuthorMapper.toEntityFromRequest(authorRequest);
        Author savedAuthor = authorRepository.save(author);
        return AuthorMapper.toDto(savedAuthor);
    }

    public AuthorDto findAuthorById(UUID authorId) {
        Author foundAuthor = authorRepository.findById(authorId)
                .orElseThrow(() -> new AuthorNotFoundException(String.format("Author not found: %s", authorId)));
        return AuthorMapper.toDto(foundAuthor);
    }

    @Transactional
    public AuthorDto updateAuthor(UUID authorId, AuthorRequest authorRequest) {
        Author foundAuthor = authorRepository.findById(authorId)
                .orElseThrow(() -> new AuthorNotFoundException(String.format("Author not found: %s", authorId)));
        AuthorMapper.updateEntity(foundAuthor, authorRequest);
        Author updatedAuthor = authorRepository.save(foundAuthor);
        return AuthorMapper.toDto(updatedAuthor);
    }

    @Transactional
    public void deleteAuthorById(UUID authorId) {
        if (!authorRepository.existsById(authorId)) {
            throw new AuthorNotFoundException(String.format("Author not found: %s", authorId));
        }
        authorRepository.deleteById(authorId);
    }

}
