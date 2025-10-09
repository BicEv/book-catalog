package ru.bicev.book_catalog.integration;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.bicev.book_catalog.entity.Author;
import ru.bicev.book_catalog.repo.AuthorRepository;
import ru.bicev.book_catalog.repo.BookRepository;
import ru.bicev.book_catalog.security.entity.User;
import ru.bicev.book_catalog.security.repo.UserRepository;
import ru.bicev.book_catalog.security.util.Role;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Author first;
    private Author second;
    private Author third;

    private User user;
    private User admin;
    private final String USERNAME = "testUser";
    private final String ADMINNAME = "testAdmin";
    private final String PASSWORD = "test_password";

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        userRepository.deleteAll();

        user = new User(null, USERNAME, passwordEncoder.encode(PASSWORD), Role.USER);
        userRepository.save(user);

        admin = new User(null, ADMINNAME, passwordEncoder.encode(PASSWORD), Role.ADMIN);
        userRepository.save(admin);

        first = authorRepository.save(new Author(UUID.randomUUID(), "Leo", "Tolstoy", 1828, "Russia"));
        second = authorRepository.save(new Author(UUID.randomUUID(), "John", "Tolkien", 1892, "UK"));
        third = authorRepository.save(new Author(UUID.randomUUID(), "ToDelete", "ToDelete", 500, "NotACounty"));
    }

    @Test
    void shouldCreateAndFetchAuthor() throws Exception {
        String token = getToken(USERNAME, PASSWORD);

        String requestJson = """
                {
                    "firstName": "Cormac",
                    "lastName": "McCarthy",
                    "birthYear": 1933,
                    "country": "USA"
                }
                """;

        mockMvc.perform(post("/api/authors")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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
    void shouldFindAllAuthors() throws Exception {
        mockMvc.perform(get("/api/authors"))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[2].firstName").value(first.getFirstName()))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void shouldUpdateAndFetchAuthor() throws Exception {
        String token = getToken(ADMINNAME, PASSWORD);

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
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
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
        String token = getToken(ADMINNAME, PASSWORD);
        UUID id = third.getId();

        mockMvc.perform(delete("/api/authors/" + id.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        assertFalse(authorRepository.findById(id).isPresent());
    }

    private String getToken(String username, String password) throws Exception {

        String loginRequest = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();

        return objectMapper
                .readTree(responseBody)
                .get("token")
                .asText();
    }

}
