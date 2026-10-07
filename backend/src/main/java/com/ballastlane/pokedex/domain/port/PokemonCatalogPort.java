package com.ballastlane.pokedex.domain.port;

import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.domain.model.PokemonSummary;

/** Outbound port to the remote Pokemon catalog (PokeAPI). */
public interface PokemonCatalogPort {

    /** @param page zero-based page index */
    PageResult<PokemonSummary> list(int page, int size);

    /**
     * @param idOrName numeric id or lower-case name
     * @throws com.ballastlane.pokedex.domain.exception.PokemonNotFoundException if it does not exist
     * @throws com.ballastlane.pokedex.domain.exception.CatalogUnavailableException if the catalog fails
     */
    Pokemon fetch(String idOrName);
}
