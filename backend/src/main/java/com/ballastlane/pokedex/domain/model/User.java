package com.ballastlane.pokedex.domain.model;

/** Application user. {@code id} is {@code null} until the user has been persisted. */
public record User(Long id, String username, String email, String passwordHash, Role role) {

    public User withId(Long newId) {
        return new User(newId, username, email, passwordHash, role);
    }
}
