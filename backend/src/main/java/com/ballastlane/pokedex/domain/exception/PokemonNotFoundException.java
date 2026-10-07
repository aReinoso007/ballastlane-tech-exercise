package com.ballastlane.pokedex.domain.exception;

public class PokemonNotFoundException extends RuntimeException {

    public PokemonNotFoundException(String identifier) {
        super("Pokemon '" + identifier + "' was not found");
    }
}
