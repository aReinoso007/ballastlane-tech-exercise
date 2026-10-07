package com.ballastlane.pokedex.application.pokemon;

import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.domain.model.PokemonEdit;
import com.ballastlane.pokedex.domain.port.PokemonRepository;
import com.ballastlane.pokedex.domain.port.TagRepository;

/** US04: modify a locally stored Pokemon. Validation is enforced by the {@link Pokemon} invariants. */
public class UpdateLocalPokemon {

    private final PokemonRepository repository;
    private final TagRepository tags;

    public UpdateLocalPokemon(PokemonRepository repository, TagRepository tags) {
        this.repository = repository;
        this.tags = tags;
    }

    /** PATCH: change only the supplied fields. */
    public Pokemon patch(int id, PokemonEdit edit) {
        Pokemon current = load(id);
        return repository.save(current.applyPartial(reuseKnownTags(edit)));
    }

    /** PUT: replace all editable fields. */
    public Pokemon replace(int id, PokemonEdit edit) {
        Pokemon current = load(id);
        return repository.save(current.replaceWith(reuseKnownTags(edit)));
    }

    /** Tags that already exist (in any letter case) keep their stored spelling; new ones are added on save. */
    private PokemonEdit reuseKnownTags(PokemonEdit edit) {
        if (edit == null || edit.tags() == null) {
            return edit;
        }
        return edit.withTags(TagNames.reuseKnownSpelling(edit.tags(), tags.findAll()));
    }

    private Pokemon load(int id) {
        return repository.findById(id).orElseThrow(() -> new PokemonNotFoundException(String.valueOf(id)));
    }
}
