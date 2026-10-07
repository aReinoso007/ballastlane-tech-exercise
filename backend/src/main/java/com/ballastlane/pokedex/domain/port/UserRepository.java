package com.ballastlane.pokedex.domain.port;

import com.ballastlane.pokedex.domain.model.User;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User save(User user);
}
