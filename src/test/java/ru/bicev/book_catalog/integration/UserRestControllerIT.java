package ru.bicev.book_catalog.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.bicev.book_catalog.security.entity.User;
import ru.bicev.book_catalog.security.repo.UserRepository;
import ru.bicev.book_catalog.security.util.Role;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private final String ADMIN_USERNAME = "admin";
    private final String TEST_USERNAME = "testUser";
    private final String TEST_USERNAME_2 = "secondTestUser";
    private final String TEST_USERNAME_3 = "thirdTestUser";
    private final String TEST_PASSWORD = "test_password";
    private User admin;
    private User second;
    private User third;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        admin = new User(null, ADMIN_USERNAME, passwordEncoder.encode(TEST_PASSWORD), Role.ADMIN);
        userRepository.save(admin);
        second = new User(null, TEST_USERNAME_2, passwordEncoder.encode(TEST_PASSWORD), Role.USER);
        userRepository.save(second);
        third = new User(null, TEST_USERNAME_3, passwordEncoder.encode(TEST_PASSWORD), Role.USER);
        userRepository.save(third);
    }

    @Test
    public void shouldRegisterUser() throws Exception {
        String userRequest = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(TEST_USERNAME, TEST_PASSWORD);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value(TEST_USERNAME));

    }

    @Test
    public void shouldReturnContflictWhenUsernameAlreadyInUse() throws Exception {
        String userRequest = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(TEST_USERNAME_2, TEST_PASSWORD);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CONFLICT"));
    }

    @Test
    public void shouldReturnUserById() throws Exception {
        String token = getToken(ADMIN_USERNAME, TEST_PASSWORD);
        Long id = admin.getId();

        mockMvc.perform(get("/api/users/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.username").value(ADMIN_USERNAME));
    }

    @Test
    public void shouldReturnForbidden() throws Exception {
        String token = getToken(TEST_USERNAME_2, TEST_PASSWORD);
        Long id = admin.getId();

        mockMvc.perform(get("/api/users/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
        ;
    }

    @Test
    public void shouldReturnAllUsers() throws Exception {
        String token = getToken(ADMIN_USERNAME, TEST_PASSWORD);

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].username").value(admin.getUsername()))
                .andExpect(jsonPath("$.content[1].username").value(second.getUsername()))
                .andExpect(jsonPath("$.content[2].username").value(third.getUsername()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true));

    }

    @Test
    public void shouldReturnForbiddenWhenGetAllUsersByNotAdmin() throws Exception {
        String token = getToken(TEST_USERNAME_2, TEST_PASSWORD);

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @Test
    public void shouldUpdatePassword() throws Exception {
        String newPass = "new_password";
        Long id = second.getId();
        String token = getToken(TEST_USERNAME_2, TEST_PASSWORD);
        String userRequest = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(TEST_USERNAME_2, newPass);

        mockMvc.perform(put("/api/users/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userRequest))
                .andExpect(status().isNoContent());

        assertNotNull(getToken(TEST_USERNAME_2, newPass));

    }

    @Test
    void shouldReturnForbiddenWhenChangingNotYoursPassword() throws Exception {
        Long id = second.getId();
        String token = getToken(TEST_USERNAME_3, TEST_PASSWORD);
        String userRequest = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(TEST_USERNAME_3, "newPass");

        mockMvc.perform(put("/api/users/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userRequest))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        Long id = second.getId();
        String token = getToken(ADMIN_USERNAME, TEST_PASSWORD);

        mockMvc.perform(delete("/api/users/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/users/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
        ;

    }

    @Test
    void shouldReturnForbiddenWhenDeleteingNotYoursAccount() throws Exception {
        Long id = third.getId();
        String token = getToken(TEST_USERNAME_2, TEST_PASSWORD);

        mockMvc.perform(delete("/api/users/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @Test
    void shouldReturnUnauthorized() throws Exception {
        Long id = second.getId();
        mockMvc.perform(delete("/api/users/" + id))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.statusCode").value(401));
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
