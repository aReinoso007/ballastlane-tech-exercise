package com.ballastlane.pokedex.application.user;

import com.ballastlane.pokedex.domain.exception.InvalidCredentialsException;
import com.ballastlane.pokedex.domain.model.User;
import com.ballastlane.pokedex.domain.port.PasswordHasher;
import com.ballastlane.pokedex.domain.port.TokenIssuer;
import com.ballastlane.pokedex.domain.port.UserRepository;

public class AuthenticateUser {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final TokenIssuer tokens;

    public AuthenticateUser(UserRepository users, PasswordHasher hasher, TokenIssuer tokens) {
        this.users = users;
        this.hasher = hasher;
        this.tokens = tokens;
    }

    public AuthResult execute(String username, String rawPassword) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isEmpty()) {
            throw new InvalidCredentialsException();
        }
        User user = users.findByUsername(username.trim().toLowerCase())
                .filter(u -> hasher.matches(rawPassword, u.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        TokenIssuer.IssuedToken token = tokens.issue(user);
        return new AuthResult(token.value(), token.expiresAt(), user);
    }
}
