package ru.bicev.book_catalog.security.controller;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import ru.bicev.book_catalog.dto.AuthResponse;
import ru.bicev.book_catalog.dto.ErrorDto;
import ru.bicev.book_catalog.dto.PagedResponse;
import ru.bicev.book_catalog.security.dto.UserDto;
import ru.bicev.book_catalog.security.dto.UserRequest;
import ru.bicev.book_catalog.security.jwt.JwtUtil;
import ru.bicev.book_catalog.security.service.UserService;
import ru.bicev.book_catalog.security.util.Role;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private static final Logger logger = LoggerFactory.getLogger(UserRestController.class);

    private final UserService userService;
    private final AuthenticationManager manager;
    private final JwtUtil jwtUtil;

    public UserRestController(UserService userService, AuthenticationManager manager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.manager = manager;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Register user", description = "Register new user and return JWT token and UserDto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Bad credentials"),
            @ApiResponse(responseCode = "409", description = "Username already in use", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    @PostMapping
    public ResponseEntity<AuthResponse> registerUser(@RequestBody @Valid UserRequest userRequest,
            @RequestParam(defaultValue = "USER") Role role) {

        logger.debug("Register request - username: {}, role: {}", userRequest.username(), role);

        UserDto createdUser = userService.registerUser(userRequest, role);
        URI location = URI.create("/api/users/" + createdUser.id());
        logger.info("User registered: {}", location);

        Authentication auth = manager.authenticate(
                new UsernamePasswordAuthenticationToken(userRequest.username(), userRequest.password()));

        String token = jwtUtil.generateToken(createdUser.username());

        AuthResponse response = new AuthResponse(token, createdUser.id(), createdUser.username());

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get user by Id", security = @SecurityRequirement(name = "bearerAuth"), description = "Find user by Id and return UserDto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found", content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Id does not match with current user or current user is not Admin", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "User was not found", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    @GetMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        UserDto foundUser = userService.getUserById(userId);
        logger.info("User retrieved: {}", userId);
        return ResponseEntity.ok().body(foundUser);
    }

    @Operation(summary = "Get all users", security = @SecurityRequirement(name = "bearerAuth"), description = "Find all users and return PagedResponse")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found", content = @Content(schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Current user is not Admin", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<UserDto>> getAllUsers(@PageableDefault(size = 10) Pageable pageable) {
        logger.info("All users retrieved");
        PagedResponse<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok().body(users);
    }

    @Operation(summary = "Change password", security = @SecurityRequirement(name = "bearerAuth"), description = "Changes password by current user or admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password was changed"),
            @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Id does not match with current user or current user is not Admin", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "User was not found", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    @PutMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Void> changePassword(@PathVariable Long userId, @RequestBody UserRequest userRequest) {
        userService.changePassword(userId, userRequest.password());
        logger.info("Password changed: {}", userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete user", security = @SecurityRequirement(name = "bearerAuth"), description = "Deletes user by himself or admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User was deleted"),
            @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "403", description = "Id does not match with current user or current user is not Admin", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "404", description = "User was not found", content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorDto.class)))
    })
    @DeleteMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        logger.info("User: {} was deleted", userId);
        return ResponseEntity.noContent().build();
    }

}
