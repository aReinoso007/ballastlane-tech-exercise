package com.ballastlane.pokedex.web.dto;

import com.ballastlane.pokedex.domain.model.PokemonSummary;
import java.util.List;

public record PokemonSummaryResponse(int id, String name, String spriteUrl, String category, int weight,
                                     List<String> abilities) {

    public static PokemonSummaryResponse from(PokemonSummary s) {
        return new PokemonSummaryResponse(s.id(), s.name(), s.spriteUrl(), s.category(), s.weight(), s.abilities());
    }
}
