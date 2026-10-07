package com.ballastlane.pokedex.application.pokemon;

import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.port.PokemonRepository;

public class DeleteLocalPokemon {

    private final PokemonRepository repository;

    public DeleteLocalPokemon(PokemonRepository repository) {
        this.repository = repository;
    }

    public void execute(int id) {
        if (!repository.deleteById(id)) {
            throw new PokemonNotFoundException(String.valueOf(id));
        }
    }
}
