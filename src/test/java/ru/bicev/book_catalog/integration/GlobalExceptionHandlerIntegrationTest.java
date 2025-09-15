package ru.bicev.book_catalog.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private UUID notExistingId = UUID.randomUUID();

    @Test
    void testAuthorNotFoundHandler() throws Exception {
        mockMvc.perform(get("/api/authors/" + notExistingId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("AUTHOR_NOT_FOUND"))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    void testBookNotFoundHandler() throws Exception {
        mockMvc.perform(get("/api/books/" + notExistingId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("BOOK_NOT_FOUND"))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    void testMethodArgumentNotValidHandler() throws Exception {
        String requestJson = """
                {
                    "firstName": "",
                    "lastName": "",
                    "birthYear": 1933,
                    "country": "USA"
                }

                """;
        mockMvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].field").isNotEmpty())
                .andExpect(jsonPath("$[0].errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$[1].field").isNotEmpty())
                .andExpect(jsonPath("$[1].errorCode").value("VALIDATION_ERROR"));

    }

    @Test
    void testMethodArgumentNotValidHandlerWithMaxBirthYear() throws Exception {
        String requestJson = """
                {
                    "firstName": "Test",
                    "lastName": "Writer",
                    "birthYear": 2077,
                    "country": "USA"
                }

                """;
        mockMvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].field").value("birthYear"))
                .andExpect(jsonPath("$[0].errorCode").value("VALIDATION_ERROR"));

    }

    @Test
    void testMethodArgumentNotValidHandlerWithMaxReleaseYear() throws Exception {
        UUID id = UUID.randomUUID();

        String requestJson = String.format("""
                {
                    "title": "Test",
                    "releaseYear": 5000,
                    "genre": "SCI_FI",
                    "authorId": "%s"
                }

                """, id);
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].field").value("releaseYear"))
                .andExpect(jsonPath("$[0].errorCode").value("VALIDATION_ERROR"));

    }

    @Test
    void testInternalServerError() throws Exception {
        UUID id = UUID.randomUUID();

        String requestJson = String.format("""
                {
                    "title": "Test",
                    "releaseYear": 1900,
                    "genre": ABBBBBBBBBBBBBB,
                    "authorId": "%s"
                }

                """, id);

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"));
    }

}
