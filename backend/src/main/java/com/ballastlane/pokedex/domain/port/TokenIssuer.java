package com.ballastlane.pokedex.domain.port;

import com.ballastlane.pokedex.domain.model.User;
import java.time.Instant;

public interface TokenIssuer {

    IssuedToken issue(User user);

    record IssuedToken(String value, Instant expiresAt) {
    }
}
