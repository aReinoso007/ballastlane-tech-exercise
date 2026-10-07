package com.ballastlane.pokedex.application.user;

import com.ballastlane.pokedex.domain.exception.InvalidDataException;
import com.ballastlane.pokedex.domain.exception.UserAlreadyExistsException;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import com.ballastlane.pokedex.domain.port.PasswordHasher;
import com.ballastlane.pokedex.domain.port.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegisterUser {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.-]{3,30}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository users;
    private final PasswordHasher hasher;

    public RegisterUser(UserRepository users, PasswordHasher hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    public User execute(String username, String email, String rawPassword) {
        String normalizedUsername = username == null ? "" : username.trim().toLowerCase();
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        validate(normalizedUsername, normalizedEmail, rawPassword);

        if (users.existsByUsername(normalizedUsername)) {
            throw new UserAlreadyExistsException("Username '" + normalizedUsername + "' is already taken");
        }
        if (users.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("Email '" + normalizedEmail + "' is already registered");
        }
        return users.save(new User(null, normalizedUsername, normalizedEmail, hasher.hash(rawPassword), Role.USER));
    }

    private static void validate(String username, String email, String password) {
        List<String> violations = new ArrayList<>();
        if (!USERNAME.matcher(username).matches()) {
            violations.add("username must be 3-30 characters: letters, digits, '_', '.' or '-'");
        }
        if (email.length() > 120 || !EMAIL.matcher(email).matches()) {
            violations.add("email must be a valid address of at most 120 characters");
        }
        if (!isStrongPassword(password)) {
            violations.add("password must be 8-72 characters and contain at least one letter and one digit");
        }
        if (!violations.isEmpty()) {
            throw new InvalidDataException(violations);
        }
    }

    private static boolean isStrongPassword(String p) {
        return p != null && p.length() >= 8 && p.length() <= 72
                && p.chars().anyMatch(Character::isLetter)
                && p.chars().anyMatch(Character::isDigit);
    }
}
