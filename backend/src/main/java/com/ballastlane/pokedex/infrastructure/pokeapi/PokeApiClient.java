package com.ballastlane.pokedex.infrastructure.pokeapi;

import com.ballastlane.pokedex.domain.exception.CatalogUnavailableException;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Thin, cached HTTP client over PokeAPI. Responses are cached (Caffeine, 1h by default) so the
 * public API stays fast and we are kind to the upstream service.
 */
public class PokeApiClient {

    private final RestClient rest;

    public PokeApiClient(RestClient rest) {
        this.rest = rest;
    }

    @Cacheable("pokeapi-page")
    public JsonNode page(int limit, int offset) {
        return get("/pokemon?limit={limit}&offset={offset}", "page", limit, offset);
    }

    @Cacheable("pokeapi-pokemon")
    public JsonNode pokemon(String idOrName) {
        return get("/pokemon/{id}", idOrName, idOrName);
    }

    @Cacheable("pokeapi-species")
    public JsonNode species(String idOrName) {
        return get("/pokemon-species/{id}", idOrName, idOrName);
    }

    @Cacheable("pokeapi-evolution")
    public JsonNode evolutionChain(int id) {
        return get("/evolution-chain/{id}", String.valueOf(id), id);
    }

    private JsonNode get(String uriTemplate, String label, Object... variables) {
        try {
            JsonNode body = rest.get().uri(uriTemplate, variables).retrieve().body(JsonNode.class);
            if (body == null || body.isMissingNode() || body.isNull()) {
                throw new CatalogUnavailableException("PokeAPI returned an empty response", null);
            }
            return body;
        } catch (HttpClientErrorException.NotFound e) {
            throw new PokemonNotFoundException(label);
        } catch (RestClientException e) {
            throw new CatalogUnavailableException("PokeAPI request failed: " + e.getMessage(), e);
        }
    }
}
