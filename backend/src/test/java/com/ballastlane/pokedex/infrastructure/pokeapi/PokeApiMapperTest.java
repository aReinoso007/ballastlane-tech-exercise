package com.ballastlane.pokedex.infrastructure.pokeapi;

import static com.ballastlane.pokedex.support.JsonFixtures.node;
import static org.assertj.core.api.Assertions.assertThat;

import com.ballastlane.pokedex.domain.model.EvolutionStage;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class PokeApiMapperTest {

    @Test
    void mapsSummary() {
        PokemonSummary s = PokeApiMapper.toSummary(node("pokemon-bulbasaur"), node("species-bulbasaur"));

        assertThat(s.id()).isEqualTo(1);
        assertThat(s.name()).isEqualTo("bulbasaur");
        assertThat(s.category()).isEqualTo("Seed Pokémon");
        assertThat(s.weight()).isEqualTo(69);
        assertThat(s.spriteUrl()).isEqualTo("https://sprites/front/1.png");
        assertThat(s.abilities()).containsExactly("overgrow", "chlorophyll");
    }

    @Test
    void mapsFullPokemonWithCleanedEnglishDescription() {
        Pokemon p = PokeApiMapper.toPokemon(node("pokemon-bulbasaur"), node("species-bulbasaur"),
                node("evolution-chain-1"));

        assertThat(p.spriteUrl()).isEqualTo("https://sprites/artwork/1.png");
        assertThat(p.description())
                .isEqualTo("A strange seed was planted on its back at birth. The plant sprouts and grows with this POKéMON.");
        assertThat(p.stats()).hasSize(6);
        assertThat(p.stats().get(3).name()).isEqualTo("special-attack");
        assertThat(p.stats().get(3).baseStat()).isEqualTo(65);
        assertThat(p.localizedName()).isNull();
        assertThat(p.tags()).isEmpty();
    }

    @Test
    void flattensLinearEvolutionChain() {
        List<EvolutionStage> stages = PokeApiMapper.flattenChain(node("evolution-chain-1"));

        assertThat(stages).extracting(EvolutionStage::name).containsExactly("bulbasaur", "ivysaur", "venusaur");
        assertThat(stages).extracting(EvolutionStage::stage).containsExactly(0, 1, 2);
        assertThat(stages).extracting(EvolutionStage::evolvesFrom).containsExactly(null, "bulbasaur", "ivysaur");
        assertThat(stages.get(2).id()).isEqualTo(3);
        assertThat(stages.get(2).spriteUrl()).endsWith("/official-artwork/3.png");
    }

    @Test
    void flattensBranchingChain() {
        List<EvolutionStage> stages = PokeApiMapper.flattenChain(node("evolution-chain-eevee"));

        assertThat(stages).extracting(EvolutionStage::name).containsExactly("eevee", "vaporeon", "jolteon");
        assertThat(stages).extracting(EvolutionStage::evolvesFrom).containsExactly(null, "eevee", "eevee");
    }

    @Test
    void handlesMissingChain() {
        assertThat(PokeApiMapper.flattenChain(null)).isEmpty();
    }

    @Test
    void fallsBackToOtherSpriteAndToleratesMissingSpeciesData() throws Exception {
        var pokemon = new ObjectMapper().readTree("""
                {"id": 9, "name": "x", "height": 1, "weight": 1,
                 "abilities": [{"ability": {"name": "a"}}],
                 "stats": [],
                 "species": {"name": "x", "url": "https://pokeapi.co/api/v2/pokemon-species/9/"},
                 "sprites": {"front_default": null, "other": {"official-artwork": {"front_default": "https://art/9.png"}}}}
                """);
        var species = new ObjectMapper().readTree("{\"genera\": [], \"flavor_text_entries\": []}");

        Pokemon p = PokeApiMapper.toPokemon(pokemon, species, null);

        assertThat(p.spriteUrl()).isEqualTo("https://art/9.png");
        assertThat(p.category()).isNull();
        assertThat(p.description()).isNull();
        assertThat(p.evolutions()).isEmpty();
    }

    @Test
    void extractsIdFromResourceUrl() {
        assertThat(PokeApiMapper.idFromUrl("https://pokeapi.co/api/v2/evolution-chain/67/")).isEqualTo(67);
        assertThat(PokeApiMapper.idFromUrl("https://pokeapi.co/api/v2/pokemon-species/133")).isEqualTo(133);
    }
}
