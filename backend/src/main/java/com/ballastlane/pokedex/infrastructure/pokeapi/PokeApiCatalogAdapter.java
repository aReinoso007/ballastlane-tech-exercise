package com.ballastlane.pokedex.infrastructure.pokeapi;

import com.ballastlane.pokedex.domain.exception.CatalogUnavailableException;
import com.ballastlane.pokedex.domain.exception.InvalidDataException;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.domain.model.PokemonName;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import com.ballastlane.pokedex.domain.port.PokemonCatalogPort;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/** {@link PokemonCatalogPort} implementation backed by PokeAPI. */
public class PokeApiCatalogAdapter implements PokemonCatalogPort {

    private final PokeApiClient client;
    private final Executor executor;

    public PokeApiCatalogAdapter(PokeApiClient client, Executor executor) {
        this.client = client;
        this.executor = executor;
    }

    @Override
    public PageResult<PokemonSummary> list(int page, int size) {
        JsonNode listing = client.page(size, page * size);
        List<CompletableFuture<PokemonSummary>> futures = new ArrayList<>();
        for (JsonNode entry : listing.path("results")) {
            String name = entry.path("name").asText();
            futures.add(CompletableFuture.supplyAsync(() -> summarize(name), executor));
        }
        List<PokemonSummary> items = futures.stream().map(PokeApiCatalogAdapter::join).toList();
        return new PageResult<>(items, page, size, listing.path("count").asLong());
    }

    @Override
    public List<PokemonName> listNames() {
        List<PokemonName> names = new ArrayList<>();
        for (JsonNode entry : client.names().path("results")) {
            try {
                int id = PokeApiMapper.idFromUrl(entry.path("url").asText());
                names.add(new PokemonName(id, entry.path("name").asText(), PokeApiMapper.spriteUrl(id)));
            } catch (NumberFormatException e) {
                // An entry without a usable id cannot be opened later; skip it rather than fail the search.
            }
        }
        return names;
    }

    @Override
    public Pokemon fetch(String idOrName) {
        JsonNode pokemon = client.pokemon(idOrName);
        JsonNode species = client.species(speciesKey(pokemon));
        JsonNode chain = null;
        JsonNode chainUrl = species.path("evolution_chain").path("url");
        if (!chainUrl.isMissingNode() && !chainUrl.isNull()) {
            chain = client.evolutionChain(PokeApiMapper.idFromUrl(chainUrl.asText()));
        }
        try {
            return PokeApiMapper.toPokemon(pokemon, species, chain);
        } catch (InvalidDataException e) {
            throw new CatalogUnavailableException("PokeAPI returned unusable data: " + e.getMessage(), e);
        }
    }

    private PokemonSummary summarize(String name) {
        JsonNode pokemon = client.pokemon(name);
        JsonNode species = client.species(speciesKey(pokemon));
        return PokeApiMapper.toSummary(pokemon, species);
    }

    private static String speciesKey(JsonNode pokemon) {
        JsonNode species = pokemon.path("species").path("name");
        return species.isMissingNode() ? pokemon.path("name").asText() : species.asText();
    }

    private static <T> T join(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw e;
        }
    }
}
