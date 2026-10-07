package com.ballastlane.pokedex.application.pokemon;

import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import com.ballastlane.pokedex.domain.port.PokemonCatalogPort;

/** US01: browse remote Pokemon with pagination. */
public class ListPokemon {

    public static final int MAX_PAGE_SIZE = 50;

    private final PokemonCatalogPort catalog;

    public ListPokemon(PokemonCatalogPort catalog) {
        this.catalog = catalog;
    }

    public PageResult<PokemonSummary> execute(int page, int size) {
        Paging.validate(page, size, MAX_PAGE_SIZE);
        return catalog.list(page, size);
    }
}
