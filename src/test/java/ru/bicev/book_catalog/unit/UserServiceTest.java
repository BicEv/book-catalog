package ru.bicev.book_catalog.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import ru.bicev.book_catalog.exception.UserNotFoundException;
import ru.bicev.book_catalog.exception.UsernameAlreadyExistsException;
import ru.bicev.book_catalog.security.auth.CustomUserDetails;
import ru.bicev.book_catalog.security.dto.UserDto;
import ru.bicev.book_catalog.security.dto.UserRequest;
import ru.bicev.book_catalog.security.entity.User;
import ru.bicev.book_catalog.security.repo.UserRepository;
import ru.bicev.book_catalog.security.service.UserService;
import ru.bicev.book_catalog.security.util.Role;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private User admin;
    private User notCurrent;
    private UserRequest userRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User(1L, "test_user", "test_password", Role.USER);
        admin = new User(2L, "test_admin", "test_password", Role.ADMIN);
        notCurrent = new User(3L, "NOT_CURRENT_USER", "blank", Role.USER);
        userRequest = new UserRequest("test_user", "test_password");
    }

    @AfterEach
    public void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createUserSuccess() {
        when(userRepository.existsByUsername("test_user")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");

        UserDto created = userService.registerUser(userRequest, Role.USER);

        assertEquals(userRequest.username(), created.username());

        verify(userRepository, times(1)).existsByUsername("test_user");
        verify(userRepository, times(1)).save(any());
        verify(passwordEncoder, times(1)).encode("test_password");
    }

    @Test
    void createUserUsernameAlreadyExists() {
        when(userRepository.existsByUsername("test_user")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.registerUser(userRequest, Role.USER));
    }

    @Test
    void getUserByIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto found = userService.getUserById(1L);

        assertEquals(user.getId(), found.id());
        assertEquals(user.getUsername(), found.username());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserByIdUserNotFoundException() {
        when(userRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(4L));
    }

    @Test
    void changePasswordSuccess() {
        authenticateAs(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new_pass")).thenReturn("encodedPass");

        userService.changePassword(1L, "new_pass");

        assertEquals("encodedPass", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changePasswordAdminSuccess() {
        authenticateAs(admin);
        when(userRepository.findById(3L)).thenReturn(Optional.of(notCurrent));
        when(passwordEncoder.encode("new_pass")).thenReturn("encodedPass");

        userService.changePassword(3L, "new_pass");

        assertEquals("encodedPass", notCurrent.getPassword());
        verify(userRepository, times(1)).save(notCurrent);
    }

    @Test
    void changePasswordAccessDeniedException() {
        authenticateAs(notCurrent);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> userService.changePassword(1L, "wont_work"));

    }

    @Test
    void deleteUserSuccess() {
        authenticateAs(user);
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUserByAdminSuccess() {
        authenticateAs(admin);
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUserByAdminUserNotFoundException() {
        authenticateAs(admin);
        when(userRepository.existsById(6L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(6L));
    }

    @Test
    void deleteUserByNotCurrentUserAccessDeniedException() {
        authenticateAs(notCurrent);
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void findAllUsersSuccess() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user, admin, notCurrent));

        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<UserDto> foundUsers = userService.getAllUsers(pageable);

        assertEquals(user.getUsername(), foundUsers.getContent().get(0).username());
        assertEquals(admin.getUsername(), foundUsers.getContent().get(1).username());
        assertEquals(notCurrent.getUsername(), foundUsers.getContent().get(2).username());

    }

}
