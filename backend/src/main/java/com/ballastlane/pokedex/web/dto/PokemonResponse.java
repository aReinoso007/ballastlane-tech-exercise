package com.ballastlane.pokedex.web.dto;

import com.ballastlane.pokedex.domain.model.Pokemon;
import java.util.List;

/** Full Pokemon representation, used for both remote detail and locally stored records. */
public record PokemonResponse(
        int id,
        String name,
        String spriteUrl,
        String category,
        String description,
        int height,
        int weight,
        List<String> abilities,
        List<StatResponse> stats,
        List<EvolutionResponse> evolutions,
        String localizedName,
        String region,
        List<String> tags) {

    public record StatResponse(String name, int baseStat) {
    }

    public record EvolutionResponse(int id, String name, String spriteUrl, int stage, String evolvesFrom) {
    }

    public static PokemonResponse from(Pokemon p) {
        return new PokemonResponse(
                p.id(), p.name(), p.spriteUrl(), p.category(), p.description(), p.height(), p.weight(),
                p.abilities(),
                p.stats().stream().map(s -> new StatResponse(s.name(), s.baseStat())).toList(),
                p.evolutions().stream()
                        .map(e -> new EvolutionResponse(e.id(), e.name(), e.spriteUrl(), e.stage(), e.evolvesFrom()))
                        .toList(),
                p.localizedName(), p.region(), p.tags());
    }
}
