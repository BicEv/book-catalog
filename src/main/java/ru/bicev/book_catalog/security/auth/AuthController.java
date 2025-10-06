package ru.bicev.book_catalog.security.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.bicev.book_catalog.security.dto.TokenDto;
import ru.bicev.book_catalog.security.dto.UserRequest;
import ru.bicev.book_catalog.security.jwt.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody UserRequest userRequest) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userRequest.username(), userRequest.password()));
            TokenDto token = new TokenDto(jwtUtil.generateToken(userRequest.username()));
            logger.debug("User logged in: {}", userRequest.username());
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            logger.error("Exception in AuthController: {}", e);
            return ResponseEntity.status(401).build();
        }
    }

}
