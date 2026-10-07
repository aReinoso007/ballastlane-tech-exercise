package com.ballastlane.pokedex.domain.port;

import java.util.List;

/**
 * Outbound port to the library of tags used so far. Tags are added to the library automatically
 * when a Pokemon carrying them is saved through {@link PokemonRepository#save}.
 */
public interface TagRepository {

    /** All known tags, alphabetically (ignoring case). */
    List<String> findAll();
}
