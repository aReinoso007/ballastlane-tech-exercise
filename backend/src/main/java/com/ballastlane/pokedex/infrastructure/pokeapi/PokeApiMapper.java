package com.ballastlane.pokedex.infrastructure.pokeapi;

import com.ballastlane.pokedex.domain.model.EvolutionStage;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import com.ballastlane.pokedex.domain.model.Stat;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;

/** Translates PokeAPI JSON documents into domain objects (anti-corruption layer). */
final class PokeApiMapper {

    private static final String ARTWORK_URL =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%d.png";

    private PokeApiMapper() {
    }

    static PokemonSummary toSummary(JsonNode pokemon, JsonNode species) {
        String sprite = text(pokemon.path("sprites").path("front_default"));
        if (sprite == null) {
            sprite = artwork(pokemon);
        }
        return new PokemonSummary(
                pokemon.path("id").asInt(),
                pokemon.path("name").asText(),
                sprite,
                category(species),
                pokemon.path("weight").asInt(),
                abilities(pokemon));
    }

    static Pokemon toPokemon(JsonNode pokemon, JsonNode species, JsonNode evolutionChain) {
        String sprite = artwork(pokemon);
        if (sprite == null) {
            sprite = text(pokemon.path("sprites").path("front_default"));
        }
        List<Stat> stats = new ArrayList<>();
        for (JsonNode s : pokemon.path("stats")) {
            stats.add(new Stat(s.path("stat").path("name").asText(), s.path("base_stat").asInt()));
        }
        return new Pokemon(
                pokemon.path("id").asInt(),
                pokemon.path("name").asText(null),
                pokemon.path("height").asInt(),
                pokemon.path("weight").asInt(),
                sprite,
                category(species),
                description(species),
                abilities(pokemon),
                stats,
                flattenChain(evolutionChain),
                null,
                null,
                List.of());
    }

    static List<EvolutionStage> flattenChain(JsonNode chainDocument) {
        List<EvolutionStage> out = new ArrayList<>();
        if (chainDocument != null && chainDocument.hasNonNull("chain")) {
            walk(chainDocument.get("chain"), 0, null, out);
        }
        return out;
    }

    static int idFromUrl(String url) {
        String trimmed = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return Integer.parseInt(trimmed.substring(trimmed.lastIndexOf('/') + 1));
    }

    private static void walk(JsonNode node, int stage, String parent, List<EvolutionStage> out) {
        String name = node.path("species").path("name").asText();
        int id = idFromUrl(node.path("species").path("url").asText());
        out.add(new EvolutionStage(id, name, ARTWORK_URL.formatted(id), stage, parent));
        for (JsonNode next : node.path("evolves_to")) {
            walk(next, stage + 1, name, out);
        }
    }

    private static List<String> abilities(JsonNode pokemon) {
        List<String> abilities = new ArrayList<>();
        for (JsonNode a : pokemon.path("abilities")) {
            String name = text(a.path("ability").path("name"));
            if (name != null) {
                abilities.add(name);
            }
        }
        return abilities;
    }

    private static String category(JsonNode species) {
        for (JsonNode g : species.path("genera")) {
            if ("en".equals(g.path("language").path("name").asText())) {
                return text(g.path("genus"));
            }
        }
        return null;
    }

    private static String description(JsonNode species) {
        for (JsonNode entry : species.path("flavor_text_entries")) {
            if ("en".equals(entry.path("language").path("name").asText())) {
                String raw = entry.path("flavor_text").asText("");
                return raw.replaceAll("[\\n\\f\\r\\u000c]+", " ").replaceAll("\\s{2,}", " ").trim();
            }
        }
        return null;
    }

    private static String artwork(JsonNode pokemon) {
        return text(pokemon.path("sprites").path("other").path("official-artwork").path("front_default"));
    }

    private static String text(JsonNode n) {
        return n.isMissingNode() || n.isNull() ? null : n.asText();
    }
}
