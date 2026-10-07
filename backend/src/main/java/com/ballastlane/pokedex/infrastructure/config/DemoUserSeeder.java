package com.ballastlane.pokedex.infrastructure.config;

import com.ballastlane.pokedex.application.user.RegisterUser;
import com.ballastlane.pokedex.domain.port.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Creates the documented demo account on startup. The password is hashed through the regular
 * {@link RegisterUser} use case, so no precomputed hash lives in a migration.
 */
@Component
@ConditionalOnProperty(name = "app.seed.demo-user.enabled", havingValue = "true")
class DemoUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);

    private final RegisterUser registerUser;
    private final UserRepository users;
    private final String username;
    private final String email;
    private final String password;

    DemoUserSeeder(RegisterUser registerUser, UserRepository users,
                   @Value("${app.seed.demo-user.username}") String username,
                   @Value("${app.seed.demo-user.email}") String email,
                   @Value("${app.seed.demo-user.password}") String password) {
        this.registerUser = registerUser;
        this.users = users;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.existsByUsername(username)) {
            return;
        }
        registerUser.execute(username, email, password);
        log.info("Seeded demo user '{}'", username);
    }
}
