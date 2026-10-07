package com.ballastlane.pokedex.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ballastlane.pokedex.domain.exception.InvalidCredentialsException;
import com.ballastlane.pokedex.domain.exception.InvalidDataException;
import com.ballastlane.pokedex.domain.exception.UserAlreadyExistsException;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import com.ballastlane.pokedex.support.FakeSecurityPorts;
import com.ballastlane.pokedex.support.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserUseCasesTest {

    private InMemoryUserRepository users;

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
    }

    @Nested
    class RegisterUserTest {

        private RegisterUser useCase;

        @BeforeEach
        void init() {
            useCase = new RegisterUser(users, FakeSecurityPorts.hasher());
        }

        @Test
        void registersUserWithHashedPasswordAndNormalizedFields() {
            User user = useCase.execute("  Alex ", "Alex@Example.com", "Password1");

            assertThat(user.id()).isNotNull();
            assertThat(user.username()).isEqualTo("alex");
            assertThat(user.email()).isEqualTo("alex@example.com");
            assertThat(user.passwordHash()).isEqualTo("hash:Password1").isNotEqualTo("Password1");
            assertThat(user.role()).isEqualTo(Role.USER);
        }

        @Test
        void rejectsDuplicateUsernameCaseInsensitively() {
            useCase.execute("alex", "a@example.com", "Password1");
            assertThatThrownBy(() -> useCase.execute("ALEX", "b@example.com", "Password1"))
                    .isInstanceOf(UserAlreadyExistsException.class);
        }

        @Test
        void rejectsDuplicateEmail() {
            useCase.execute("alex", "a@example.com", "Password1");
            assertThatThrownBy(() -> useCase.execute("bob", "A@example.com", "Password1"))
                    .isInstanceOf(UserAlreadyExistsException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"ab", "has space", "bad$char", "this-username-is-way-too-long-to-accept"})
        void rejectsInvalidUsernames(String username) {
            assertThatThrownBy(() -> useCase.execute(username, "a@example.com", "Password1"))
                    .isInstanceOf(InvalidDataException.class)
                    .hasMessageContaining("username");
        }

        @ParameterizedTest
        @ValueSource(strings = {"not-an-email", "a@b", "@example.com", ""})
        void rejectsInvalidEmails(String email) {
            assertThatThrownBy(() -> useCase.execute("alex", email, "Password1"))
                    .isInstanceOf(InvalidDataException.class)
                    .hasMessageContaining("email");
        }

        @ParameterizedTest
        @ValueSource(strings = {"short1", "allletters", "12345678", ""})
        void rejectsWeakPasswords(String password) {
            assertThatThrownBy(() -> useCase.execute("alex", "a@example.com", password))
                    .isInstanceOf(InvalidDataException.class)
                    .hasMessageContaining("password");
        }

        @Test
        void collectsAllViolations() {
            assertThatThrownBy(() -> useCase.execute("a", "bad", "x"))
                    .isInstanceOf(InvalidDataException.class)
                    .satisfies(e -> assertThat(((InvalidDataException) e).violations()).hasSize(3));
        }
    }

    @Nested
    class AuthenticateUserTest {

        private AuthenticateUser useCase;

        @BeforeEach
        void init() {
            new RegisterUser(users, FakeSecurityPorts.hasher()).execute("alex", "a@example.com", "Password1");
            useCase = new AuthenticateUser(users, FakeSecurityPorts.hasher(), FakeSecurityPorts.tokenIssuer());
        }

        @Test
        void returnsTokenForValidCredentials() {
            AuthResult result = useCase.execute(" Alex ", "Password1");

            assertThat(result.token()).isEqualTo("token-for-alex");
            assertThat(result.user().username()).isEqualTo("alex");
            assertThat(result.expiresAt()).isNotNull();
        }

        @Test
        void rejectsWrongPassword() {
            assertThatThrownBy(() -> useCase.execute("alex", "Wrong123"))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        void rejectsUnknownUserWithSameError() {
            assertThatThrownBy(() -> useCase.execute("ghost", "Password1"))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        void rejectsBlankInput() {
            assertThatThrownBy(() -> useCase.execute("", ""))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
    }
}
