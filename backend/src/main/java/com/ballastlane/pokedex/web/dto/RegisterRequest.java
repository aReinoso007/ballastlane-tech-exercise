package com.ballastlane.pokedex.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 60) String username,
        @NotBlank @Size(max = 160) String email,
        @NotBlank @Size(max = 200) String password) {
}
