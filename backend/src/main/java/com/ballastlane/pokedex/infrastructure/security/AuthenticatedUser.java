package com.ballastlane.pokedex.infrastructure.security;

import com.ballastlane.pokedex.domain.model.Role;

public record AuthenticatedUser(Long id, String username, Role role) {
}
