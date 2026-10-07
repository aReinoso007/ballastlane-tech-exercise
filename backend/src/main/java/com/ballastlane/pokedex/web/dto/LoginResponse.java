package com.ballastlane.pokedex.web.dto;

import com.ballastlane.pokedex.application.user.AuthResult;
import java.time.Instant;

public record LoginResponse(String token, String tokenType, Instant expiresAt, UserResponse user) {

    public static LoginResponse from(AuthResult result) {
        return new LoginResponse(result.token(), "Bearer", result.expiresAt(), UserResponse.from(result.user()));
    }
}
