package com.ballastlane.pokedex.web.dto;

import com.ballastlane.pokedex.domain.model.PokemonEdit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/** PATCH body: every field is optional, only provided ones are changed. */
public record PokemonPatchRequest(
        @Size(min = 1, max = 100) String name,
        @Positive Integer height,
        @Positive Integer weight,
        @Size(max = 100) String category,
        @Size(max = 2000) String description,
        @Size(min = 1, max = 10) List<@NotBlank @Size(max = 60) String> abilities,
        @Size(max = 100) String localizedName,
        @Size(max = 50) String region,
        @Size(max = 10) List<@NotBlank @Size(max = 30) String> tags) {

    public PokemonEdit toEdit() {
        return new PokemonEdit(name, height, weight, category, description, abilities, localizedName, region, tags);
    }
}
