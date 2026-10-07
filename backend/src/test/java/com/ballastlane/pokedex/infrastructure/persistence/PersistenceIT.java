package com.ballastlane.pokedex.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.domain.model.PokemonEdit;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.User;
import com.ballastlane.pokedex.support.Fixtures;
import com.ballastlane.pokedex.support.SharedPostgres;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({PokemonRepositoryAdapter.class, UserRepositoryAdapter.class})
class PersistenceIT {

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        SharedPostgres.register(registry);
    }

    @Autowired
    PokemonRepositoryAdapter pokemon;

    @Autowired
    UserRepositoryAdapter users;

    @Test
    void savesAndLoadsAllPokemonDataIncludingCollectionsInOrder() {
        pokemon.save(Fixtures.bulbasaur());

        Pokemon loaded = pokemon.findById(1).orElseThrow();

        assertThat(loaded).isEqualTo(Fixtures.bulbasaur());
        assertThat(loaded.abilities()).containsExactly("overgrow", "chlorophyll");
        assertThat(loaded.evolutions()).hasSize(2);
    }

    @Test
    void updatingReplacesCollectionsAndCustomFields() {
        pokemon.save(Fixtures.bulbasaur());
        Pokemon edited = Fixtures.bulbasaur().applyPartial(PokemonEdit.builder()
                .abilities(List.of("overgrow")).region("Kanto").localizedName("Bulbi").tags(List.of("starter", "grass"))
                .build());

        pokemon.save(edited);
        Pokemon loaded = pokemon.findById(1).orElseThrow();

        assertThat(loaded.abilities()).containsExactly("overgrow");
        assertThat(loaded.region()).isEqualTo("Kanto");
        assertThat(loaded.localizedName()).isEqualTo("Bulbi");
        assertThat(loaded.tags()).containsExactly("starter", "grass");
        assertThat(pokemon.findAll(0, 10).totalItems()).isEqualTo(1);
    }

    @Test
    void paginatesOrderedById() {
        pokemon.save(Fixtures.pikachu());
        pokemon.save(Fixtures.bulbasaur());

        PageResult<Pokemon> first = pokemon.findAll(0, 1);
        PageResult<Pokemon> second = pokemon.findAll(1, 1);

        assertThat(first.items()).extracting(Pokemon::id).containsExactly(1);
        assertThat(second.items()).extracting(Pokemon::id).containsExactly(25);
        assertThat(first.totalItems()).isEqualTo(2);
        assertThat(first.totalPages()).isEqualTo(2);
    }

    @Test
    void existsAndDelete() {
        pokemon.save(Fixtures.pikachu());

        assertThat(pokemon.existsById(25)).isTrue();
        assertThat(pokemon.deleteById(25)).isTrue();
        assertThat(pokemon.existsById(25)).isFalse();
        assertThat(pokemon.deleteById(25)).isFalse();
        assertThat(pokemon.findById(25)).isEmpty();
    }

    @Test
    void persistsAndFindsUsers() {
        User saved = users.save(new User(null, "alex", "alex@example.com", "hash", Role.USER));

        assertThat(saved.id()).isNotNull();
        assertThat(users.findByUsername("alex")).contains(saved);
        assertThat(users.existsByUsername("alex")).isTrue();
        assertThat(users.existsByEmail("alex@example.com")).isTrue();
        assertThat(users.existsByUsername("nobody")).isFalse();
        assertThat(users.findByUsername("nobody")).isEmpty();
    }

    @Test
    void databaseEnforcesUniqueUsername() {
        users.save(new User(null, "alex", "a@example.com", "hash", Role.USER));

        assertThatThrownBy(() -> users.save(new User(null, "alex", "b@example.com", "hash", Role.USER)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
