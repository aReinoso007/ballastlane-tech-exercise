package com.ballastlane.pokedex.domain.model;

import java.util.List;

/** Lightweight projection used by the paginated catalog listing. */
public record PokemonSummary(int id, String name, String spriteUrl, String category, int weight,
                             List<String> abilities) {

    public PokemonSummary {
        abilities = abilities == null ? List.of() : List.copyOf(abilities);
    }
}
