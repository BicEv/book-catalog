package ru.bicev.book_catalog.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank @Size(min = 6, max = 14) String username,
        @NotBlank @Size(min = 6, max = 14) String password) {

}
