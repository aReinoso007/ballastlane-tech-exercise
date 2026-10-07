package com.ballastlane.pokedex.application.user;

import com.ballastlane.pokedex.domain.model.User;
import java.time.Instant;

public record AuthResult(String token, Instant expiresAt, User user) {
}
