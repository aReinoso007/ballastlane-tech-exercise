package com.ballastlane.pokedex.application.pokemon;

import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.domain.port.PokemonRepository;

public class ListLocalPokemon {

    public static final int MAX_PAGE_SIZE = 100;

    private final PokemonRepository repository;

    public ListLocalPokemon(PokemonRepository repository) {
        this.repository = repository;
    }

    public PageResult<Pokemon> execute(int page, int size) {
        Paging.validate(page, size, MAX_PAGE_SIZE);
        return repository.findAll(page, size);
    }
}
