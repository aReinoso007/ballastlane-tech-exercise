package com.ballastlane.pokedex.domain.exception;

public class PokemonAlreadySyncedException extends RuntimeException {

    public PokemonAlreadySyncedException(int id) {
        super("Pokemon with id " + id + " is already stored locally");
    }
}
