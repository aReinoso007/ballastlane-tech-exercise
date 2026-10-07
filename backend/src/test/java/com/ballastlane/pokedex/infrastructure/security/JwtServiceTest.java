package com.ballastlane.pokedex.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import com.ballastlane.pokedex.domain.port.TokenIssuer.IssuedToken;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-1234";
    private static final User USER = new User(7L, "alex", "a@example.com", "hash", Role.USER);
    private static final Instant NOW = Instant.parse("2030-01-01T10:00:00Z");

    private JwtService serviceAt(Instant instant, String secret) {
        return new JwtService(secret, Duration.ofMinutes(30), Clock.fixed(instant, ZoneOffset.UTC));
    }

    @Test
    void roundTripsIdentityAndExpiry() {
        JwtService service = serviceAt(NOW, SECRET);

        IssuedToken token = service.issue(USER);
        var parsed = service.parse(token.value());

        assertThat(token.expiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(30)));
        assertThat(parsed).contains(new AuthenticatedUser(7L, "alex", Role.USER));
    }

    @Test
    void rejectsExpiredToken() {
        String token = serviceAt(NOW, SECRET).issue(USER).value();

        assertThat(serviceAt(NOW.plus(Duration.ofMinutes(31)), SECRET).parse(token)).isEmpty();
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String token = serviceAt(NOW, "another-secret-another-secret-123456").issue(USER).value();

        assertThat(serviceAt(NOW, SECRET).parse(token)).isEmpty();
    }

    @Test
    void rejectsGarbage() {
        assertThat(serviceAt(NOW, SECRET).parse("not.a.jwt")).isEmpty();
        assertThat(serviceAt(NOW, SECRET).parse("")).isEmpty();
    }

    @Test
    void refusesWeakSecret() {
        assertThatThrownBy(() -> serviceAt(NOW, "short")).isInstanceOf(IllegalStateException.class);
    }
}
