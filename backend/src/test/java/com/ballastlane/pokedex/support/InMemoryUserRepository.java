package com.ballastlane.pokedex.support;

import com.ballastlane.pokedex.domain.model.User;
import com.ballastlane.pokedex.domain.port.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> byUsername = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(byUsername.get(username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return byUsername.containsKey(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return byUsername.values().stream().anyMatch(u -> u.email().equals(email));
    }

    @Override
    public User save(User user) {
        User saved = user.id() == null ? user.withId(sequence.incrementAndGet()) : user;
        byUsername.put(saved.username(), saved);
        return saved;
    }
}
