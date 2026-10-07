package com.ballastlane.pokedex.infrastructure.security;

import com.ballastlane.pokedex.domain.port.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;

public class BcryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder encoder;

    public BcryptPasswordHasher(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hash) {
        return encoder.matches(rawPassword, hash);
    }
}
