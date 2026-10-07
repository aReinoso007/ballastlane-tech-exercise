package com.ballastlane.pokedex.domain.model;

/** Entry of the lightweight catalog index used for name search and suggestions. */
public record PokemonName(int id, String name, String spriteUrl) {
}
