package com.ballastlane.pokedex.application.pokemon;

import com.ballastlane.pokedex.domain.exception.PokemonAlreadySyncedException;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.domain.port.PokemonCatalogPort;
import com.ballastlane.pokedex.domain.port.PokemonRepository;

/** US03: replicate a remote Pokemon into the local store. */
public class SyncPokemon {

    private final PokemonCatalogPort catalog;
    private final PokemonRepository repository;

    public SyncPokemon(PokemonCatalogPort catalog, PokemonRepository repository) {
        this.catalog = catalog;
        this.repository = repository;
    }

    public Pokemon execute(String idOrName) {
        Pokemon remote = catalog.fetch(Paging.normalizeIdentifier(idOrName));
        if (repository.existsById(remote.id())) {
            throw new PokemonAlreadySyncedException(remote.id());
        }
        return repository.save(remote);
    }
}
