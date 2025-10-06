package ru.bicev.book_catalog.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import ru.bicev.book_catalog.entity.Book;
import ru.bicev.book_catalog.repo.AuthorRepository;
import ru.bicev.book_catalog.repo.BookRepository;
import ru.bicev.book_catalog.security.entity.User;
import ru.bicev.book_catalog.security.repo.UserRepository;
import ru.bicev.book_catalog.security.util.Role;
import ru.bicev.book_catalog.util.Genre;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookControllerIT {

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
    private Book one;
    private Book two;
    private Book three;
    private Book four;
    private Book five;
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

        first = authorRepository.save(new Author(UUID.randomUUID(), "First", "Author", 1900, "One"));
        second = authorRepository.save(new Author(UUID.randomUUID(), "Second", "Writer", 1969, "Two"));

        one = bookRepository.save(new Book(UUID.randomUUID(), "Book one", 1920, Genre.CLASSICS, first));
        two = bookRepository.save(new Book(UUID.randomUUID(), "Book two", 1925, Genre.ROMANCE, first));
        three = bookRepository.save(new Book(UUID.randomUUID(), "Book three", 1930, Genre.SCI_FI, first));

        four = bookRepository.save(new Book(UUID.randomUUID(), "Bk one", 1990, Genre.ROMANCE, second));
        five = bookRepository.save(new Book(UUID.randomUUID(), "Bk two", 1995, Genre.SCI_FI, second));

    }

    @Test
    void shouldCreateAndFetchBook() throws Exception {
        String token = getToken(USERNAME, PASSWORD);

        UUID authorId = first.getId();
        String requestJson = String.format("""
                {
                    "title": "Test book",
                    "releaseYear": 1940,
                    "genre": "THRILLER",
                    "authorId": "%s"
                }
                """, authorId.toString());

        mockMvc.perform(post("/api/books")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Test book"))
                .andExpect(jsonPath("$.releaseYear").value(1940))
                .andExpect(jsonPath("$.genre").value("THRILLER"))
                .andExpect(jsonPath("$.author.id").value(first.getId().toString()))
                .andExpect(jsonPath("$.author.firstName").value(first.getFirstName()))
                .andExpect(jsonPath("$.author.lastName").value(first.getLastName()))
                .andExpect(jsonPath("$.author.birthYear").value(first.getBirthYear()))
                .andExpect(jsonPath("$.author.country").value(first.getCountry()));

    }

    @Test
    void shouldFindBookById() throws Exception {
        UUID id = one.getId();

        mockMvc.perform(get("/api/books/" + id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value(one.getTitle()))
                .andExpect(jsonPath("$.releaseYear").value(one.getReleaseYear()))
                .andExpect(jsonPath("$.genre").value(one.getGenre().toString()))
                .andExpect(jsonPath("$.author.id").value(first.getId().toString()))
                .andExpect(jsonPath("$.author.firstName").value(first.getFirstName()))
                .andExpect(jsonPath("$.author.lastName").value(first.getLastName()))
                .andExpect(jsonPath("$.author.birthYear").value(first.getBirthYear()))
                .andExpect(jsonPath("$.author.country").value(first.getCountry()));
    }

    @Test
    void shouldUpdateAndFetchBook() throws Exception {
        String token = getToken(ADMINNAME, PASSWORD);

        UUID bookId = four.getId();
        UUID authorId = second.getId();
        String requestJson = String.format("""
                {
                    "title": "Test book",
                    "releaseYear": 1940,
                    "genre": "THRILLER",
                    "authorId": "%s"
                }
                """, authorId.toString());

        mockMvc.perform(put("/api/books/" + bookId.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId.toString()))
                .andExpect(jsonPath("$.title").value("Test book"))
                .andExpect(jsonPath("$.releaseYear").value(1940))
                .andExpect(jsonPath("$.genre").value(Genre.THRILLER.toString()))
                .andExpect(jsonPath("$.author.id").value(second.getId().toString()))
                .andExpect(jsonPath("$.author.firstName").value(second.getFirstName()))
                .andExpect(jsonPath("$.author.lastName").value(second.getLastName()))
                .andExpect(jsonPath("$.author.birthYear").value(second.getBirthYear()))
                .andExpect(jsonPath("$.author.country").value(second.getCountry()));
    }

    @Test
    void shouldDeleteBook() throws Exception {
        String token = getToken(ADMINNAME, PASSWORD);

        UUID id = five.getId();

        mockMvc.perform(delete("/api/books/" + id.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        assertTrue(bookRepository.findById(id).isEmpty());
    }

    @Test
    void shouldReturnPagedBooksWithTitle() throws Exception {
        String title = "Book";

        mockMvc.perform(get("/api/books")
                .param("title", title)
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value(one.getTitle()))
                .andExpect(jsonPath("$.content[0].author.id").value(one.getAuthor().getId().toString()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(false));

    }

    @Test
    void shouldReturnPagedBooksWithAuthorId() throws Exception {

        String authorId = second.getId().toString();

        mockMvc.perform(get("/api/books")
                .param("authorId", authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value(four.getTitle()))
                .andExpect(jsonPath("$.content[0].author.id").value(four.getAuthor().getId().toString()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

    }

    @Test
    void shouldReturnPagedBooksWithReleaseYearRange() throws Exception {

        mockMvc.perform(get("/api/books")
                .param("startYear", "1800")
                .param("endYear", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value(four.getTitle()))
                .andExpect(jsonPath("$.content[0].author.id").value(four.getAuthor().getId().toString()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

    }

    @Test
    void shouldReturnPagedBooksWithGenre() throws Exception {

        mockMvc.perform(get("/api/books")
                .param("genre", "SCI_FI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value(five.getTitle()))
                .andExpect(jsonPath("$.content[0].author.id").value(five.getAuthor().getId().toString()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

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
