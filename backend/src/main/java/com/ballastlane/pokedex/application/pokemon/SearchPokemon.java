package com.ballastlane.pokedex.application.pokemon;

import com.ballastlane.pokedex.domain.exception.InvalidDataException;
import com.ballastlane.pokedex.domain.model.PokemonName;
import com.ballastlane.pokedex.domain.model.PokemonNameMatcher;
import com.ballastlane.pokedex.domain.port.PokemonCatalogPort;
import java.util.ArrayList;
import java.util.List;

/** Typo-tolerant name search over the catalog, used for suggestions in the search box. */
public class SearchPokemon {

    public static final int MAX_QUERY_LENGTH = 50;
    public static final int MAX_LIMIT = 20;

    private final PokemonCatalogPort catalog;

    public SearchPokemon(PokemonCatalogPort catalog) {
        this.catalog = catalog;
    }

    public List<PokemonName> execute(String query, int limit) {
        List<String> violations = new ArrayList<>();
        if (query != null && query.length() > MAX_QUERY_LENGTH) {
            violations.add("query must be at most " + MAX_QUERY_LENGTH + " characters");
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            violations.add("limit must be between 1 and " + MAX_LIMIT);
        }
        if (!violations.isEmpty()) {
            throw new InvalidDataException(violations);
        }
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return PokemonNameMatcher.rank(query, catalog.listNames(), limit);
    }
}
