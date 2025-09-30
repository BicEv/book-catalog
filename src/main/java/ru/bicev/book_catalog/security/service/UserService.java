package ru.bicev.book_catalog.security.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import ru.bicev.book_catalog.exception.UserNotFoundException;
import ru.bicev.book_catalog.exception.UsernameAlreadyExistsException;
import ru.bicev.book_catalog.security.auth.CustomUserDetails;
import ru.bicev.book_catalog.security.dto.UserDto;
import ru.bicev.book_catalog.security.dto.UserRequest;
import ru.bicev.book_catalog.security.entity.User;
import ru.bicev.book_catalog.security.repo.UserRepository;
import ru.bicev.book_catalog.security.util.Role;

// I'm using security context in this service to validate a user within the business logic rather than in controller layer
// This is not recommended architecture, but is used purely for educational purposes

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDto registerUser(UserRequest userRequest, Role role) {
        if (userRepository.existsByUsername(userRequest.username())) {
            throw new UsernameAlreadyExistsException("Username already in use");
        }
        String encodedPassword = passwordEncoder.encode(userRequest.password());

        User user = User.builder()
                .username(userRequest.username())
                .password(encodedPassword)
                .role(role)
                .build();

        User savedUser = userRepository.save(user);

        return new UserDto(savedUser.getId(), userRequest.username());
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return new UserDto(user.getId(), user.getUsername());
    }

    @Transactional
    public void changePassword(Long userId, String newPassword) {
        if (!isCurrentUserOrAdmin(userId)) {
            throw new AccessDeniedException("You don't have permission to do this");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!isCurrentUserOrAdmin(userId)) {
            throw new AccessDeniedException("You don't have permission to do this");
        }
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(userId);
    }

    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(user -> new UserDto(user.getId(), user.getUsername()));
    }

    private boolean isCurrentUserOrAdmin(Long targetId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails currentUser = (CustomUserDetails) auth.getPrincipal();

        return currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                || currentUser.getId().equals(targetId);

    }

}
