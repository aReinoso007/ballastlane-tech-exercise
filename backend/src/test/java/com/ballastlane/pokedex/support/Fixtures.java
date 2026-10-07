package com.ballastlane.pokedex.support;

import com.ballastlane.pokedex.domain.model.EvolutionStage;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import com.ballastlane.pokedex.domain.model.Role;
import com.ballastlane.pokedex.domain.model.Stat;
import com.ballastlane.pokedex.domain.model.User;
import java.util.List;

/** Shared test data builders. */
public final class Fixtures {

    private Fixtures() {
    }

    public static Pokemon bulbasaur() {
        return new Pokemon(
                1,
                "bulbasaur",
                7,
                69,
                "https://img/1.png",
                "Seed Pokémon",
                "A strange seed was planted on its back at birth.",
                List.of("overgrow", "chlorophyll"),
                List.of(new Stat("hp", 45), new Stat("attack", 49)),
                List.of(
                        new EvolutionStage(1, "bulbasaur", "https://img/1.png", 0, null),
                        new EvolutionStage(2, "ivysaur", "https://img/2.png", 1, "bulbasaur")),
                null,
                null,
                List.of());
    }

    public static Pokemon pikachu() {
        return new Pokemon(
                25,
                "pikachu",
                4,
                60,
                "https://img/25.png",
                "Mouse Pokémon",
                "When several of these gather, their electricity could build.",
                List.of("static", "lightning-rod"),
                List.of(new Stat("hp", 35), new Stat("speed", 90)),
                List.of(),
                null,
                null,
                List.of());
    }

    public static PokemonSummary summaryOf(Pokemon p) {
        return new PokemonSummary(p.id(), p.name(), p.spriteUrl(), p.category(), p.weight(), p.abilities());
    }

    public static User newUser(String username) {
        return new User(null, username, username + "@example.com", "hash:Password1", Role.USER);
    }
}
