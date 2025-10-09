package ru.bicev.book_catalog.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.bicev.book_catalog.dto.AuthorDto;
import ru.bicev.book_catalog.dto.AuthorRequest;
import ru.bicev.book_catalog.dto.PagedResponse;
import ru.bicev.book_catalog.entity.Author;
import ru.bicev.book_catalog.exception.AuthorNotFoundException;
import ru.bicev.book_catalog.repo.AuthorRepository;
import ru.bicev.book_catalog.util.AuthorMapper;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthorService.class);

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Transactional
    public AuthorDto createAuthor(AuthorRequest authorRequest) {
        logger.info("Creating author: {}", authorRequest.lastName());
        Author author = AuthorMapper.toEntityFromRequest(authorRequest);
        Author savedAuthor = authorRepository.save(author);
        logger.info("Author created: {}", savedAuthor.getId());
        return AuthorMapper.toDto(savedAuthor);
    }

    public AuthorDto findAuthorById(UUID authorId) {
        Author foundAuthor = authorRepository.findById(authorId)
                .orElseThrow(() -> new AuthorNotFoundException(String.format("Author not found: %s", authorId)));
        logger.debug("Fetched author by id {}: {}", authorId, foundAuthor);
        return AuthorMapper.toDto(foundAuthor);
    }

    public PagedResponse<AuthorDto> findAll(Pageable pageable) {
        Page<AuthorDto> page = authorRepository.findAll(pageable).map(AuthorMapper::toDto);
        PagedResponse<AuthorDto> result = new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
        logger.debug("Fetched all authors - page number: {}, total pages: {}", page.getNumber(), page.getTotalPages());
        return result;
    }

    @Transactional
    public AuthorDto updateAuthor(UUID authorId, AuthorRequest authorRequest) {
        logger.info("Updating author: {}", authorId);
        Author foundAuthor = authorRepository.findById(authorId)
                .orElseThrow(() -> new AuthorNotFoundException(String.format("Author not found: %s", authorId)));
        AuthorMapper.updateEntity(foundAuthor, authorRequest);
        Author updatedAuthor = authorRepository.save(foundAuthor);
        logger.info("Author updated: {}", updatedAuthor.getId());
        return AuthorMapper.toDto(updatedAuthor);
    }

    @Transactional
    public void deleteAuthorById(UUID authorId) {
        if (!authorRepository.existsById(authorId)) {
            throw new AuthorNotFoundException(String.format("Author not found: %s", authorId));
        }
        authorRepository.deleteById(authorId);
        logger.info("Author: {} was deleted", authorId);
    }

}
