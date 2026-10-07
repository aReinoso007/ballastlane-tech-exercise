package com.ballastlane.pokedex.application.pokemon;

import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.domain.port.PokemonRepository;

public class GetLocalPokemon {

    private final PokemonRepository repository;

    public GetLocalPokemon(PokemonRepository repository) {
        this.repository = repository;
    }

    public Pokemon execute(int id) {
        return repository.findById(id).orElseThrow(() -> new PokemonNotFoundException(String.valueOf(id)));
    }
}
