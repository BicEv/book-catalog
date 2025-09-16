package ru.bicev.book_catalog.integration;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import ru.bicev.book_catalog.entity.Author;
import ru.bicev.book_catalog.repo.AuthorRepository;
import ru.bicev.book_catalog.repo.BookRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthorControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    private Author first;
    private Author second;
    private Author third;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        first = authorRepository.save(new Author(UUID.randomUUID(), "Leo", "Tolstoy", 1828, "Russia"));
        second = authorRepository.save(new Author(UUID.randomUUID(), "John", "Tolkien", 1892, "UK"));
        third = authorRepository.save(new Author(UUID.randomUUID(), "ToDelete", "ToDelete", 500, "NotACounty"));
    }

    @Test
    void shouldCreateAndFetchAuthor() throws Exception {
        String requestJson = """
                {
                    "firstName": "Cormac",
                    "lastName": "McCarthy",
                    "birthYear": 1933,
                    "country": "USA"
                }
                """;

        mockMvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.firstName").value("Cormac"))
                .andExpect(jsonPath("$.lastName").value("McCarthy"))
                .andExpect(jsonPath("$.birthYear").value(1933))
                .andExpect(jsonPath("$.country").value("USA"));

    }

    @Test
    void shouldFindAuthorById() throws Exception {
        UUID id = first.getId();

        mockMvc.perform(get("/api/authors/" + id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(first.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(first.getLastName()))
                .andExpect(jsonPath("$.birthYear").value(first.getBirthYear()))
                .andExpect(jsonPath("$.country").value(first.getCountry()));
    }

    @Test
    void shouldUpdateAndFetchAuthor() throws Exception {
        UUID id = second.getId();

        String requestJson = """
                {
                    "firstName": "Fyodor",
                    "lastName": "Dostoevsky",
                    "birthYear": 1821,
                    "country": "Russia"
                }
                """;

        mockMvc.perform(put("/api/authors/" + id.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Fyodor"))
                .andExpect(jsonPath("$.lastName").value("Dostoevsky"))
                .andExpect(jsonPath("$.birthYear").value(1821))
                .andExpect(jsonPath("$.country").value("Russia"));

    }

    @Test
    void shouldDeleteAuthor() throws Exception {
        UUID id = third.getId();

        mockMvc.perform(delete("/api/authors/" + id.toString()))
                .andExpect(status().isNoContent());

        assertFalse(authorRepository.findById(id).isPresent());
    }

}
