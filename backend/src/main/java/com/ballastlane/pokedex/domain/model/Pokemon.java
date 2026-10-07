package com.ballastlane.pokedex.domain.model;

import com.ballastlane.pokedex.domain.exception.InvalidDataException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Core aggregate. Invariants are enforced on construction so an invalid Pokemon can never exist,
 * regardless of whether it came from PokeAPI, the database or a client request.
 *
 * <p>{@code localizedName}, {@code region} and {@code tags} are the proprietary fields that motivate
 * replicating Pokemon locally.
 */
public record Pokemon(
        int id,
        String name,
        int height,
        int weight,
        String spriteUrl,
        String category,
        String description,
        List<String> abilities,
        List<Stat> stats,
        List<EvolutionStage> evolutions,
        String localizedName,
        String region,
        List<String> tags) {

    public static final int MAX_ABILITIES = 10;
    public static final int MAX_TAGS = 10;
    public static final int MAX_TAG_LENGTH = 30;

    public Pokemon {
        name = trimToNull(name);
        spriteUrl = trimToNull(spriteUrl);
        category = trimToNull(category);
        description = trimToNull(description);
        localizedName = trimToNull(localizedName);
        region = trimToNull(region);
        abilities = cleanList(abilities);
        tags = cleanList(tags);
        stats = stats == null ? List.of() : List.copyOf(stats);
        evolutions = evolutions == null ? List.of() : List.copyOf(evolutions);
        validate(id, name, height, weight, category, description, abilities, stats, localizedName, region, tags);
    }

    /** Applies only the provided fields (PATCH semantics). */
    public Pokemon applyPartial(PokemonEdit edit) {
        if (edit == null || edit.isEmpty()) {
            throw new InvalidDataException("at least one editable field must be provided");
        }
        return new Pokemon(
                id,
                edit.name() != null ? edit.name() : name,
                edit.height() != null ? edit.height() : height,
                edit.weight() != null ? edit.weight() : weight,
                spriteUrl,
                edit.category() != null ? edit.category() : category,
                edit.description() != null ? edit.description() : description,
                edit.abilities() != null ? edit.abilities() : abilities,
                stats,
                evolutions,
                edit.localizedName() != null ? edit.localizedName() : localizedName,
                edit.region() != null ? edit.region() : region,
                edit.tags() != null ? edit.tags() : tags);
    }

    /**
     * Replaces all editable fields (PUT semantics). Mandatory fields must be present; the optional
     * custom fields are cleared when omitted.
     */
    public Pokemon replaceWith(PokemonEdit edit) {
        List<String> missing = new ArrayList<>();
        if (edit.name() == null) {
            missing.add("name is required");
        }
        if (edit.height() == null) {
            missing.add("height is required");
        }
        if (edit.weight() == null) {
            missing.add("weight is required");
        }
        if (edit.abilities() == null) {
            missing.add("abilities is required");
        }
        if (!missing.isEmpty()) {
            throw new InvalidDataException(missing);
        }
        return new Pokemon(
                id,
                edit.name(),
                edit.height(),
                edit.weight(),
                spriteUrl,
                edit.category(),
                edit.description(),
                edit.abilities(),
                stats,
                evolutions,
                edit.localizedName(),
                edit.region(),
                edit.tags());
    }

    private static void validate(int id, String name, int height, int weight, String category, String description,
                                 List<String> abilities, List<Stat> stats, String localizedName, String region,
                                 List<String> tags) {
        List<String> v = new ArrayList<>();
        if (id <= 0) {
            v.add("id must be positive");
        }
        if (name == null) {
            v.add("name must not be blank");
        } else if (name.length() > 100) {
            v.add("name must be at most 100 characters");
        }
        if (height <= 0) {
            v.add("height must be positive");
        }
        if (weight <= 0) {
            v.add("weight must be positive");
        }
        if (category != null && category.length() > 100) {
            v.add("category must be at most 100 characters");
        }
        if (description != null && description.length() > 2000) {
            v.add("description must be at most 2000 characters");
        }
        if (abilities.isEmpty() || abilities.size() > MAX_ABILITIES) {
            v.add("abilities must contain between 1 and " + MAX_ABILITIES + " entries");
        }
        if (abilities.stream().anyMatch(a -> a.length() > 60)) {
            v.add("abilities entries must be at most 60 characters");
        }
        for (Stat s : stats) {
            if (s.baseStat() < 0 || s.baseStat() > 255) {
                v.add("stat '" + s.name() + "' base value must be between 0 and 255");
            }
        }
        if (localizedName != null && localizedName.length() > 100) {
            v.add("localizedName must be at most 100 characters");
        }
        if (region != null && region.length() > 50) {
            v.add("region must be at most 50 characters");
        }
        if (tags.size() > MAX_TAGS || tags.stream().anyMatch(t -> t.length() > MAX_TAG_LENGTH)) {
            v.add("tags must contain at most " + MAX_TAGS + " entries of up to " + MAX_TAG_LENGTH + " characters");
        }
        if (!v.isEmpty()) {
            throw new InvalidDataException(v);
        }
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static List<String> cleanList(List<String> in) {
        if (in == null) {
            return List.of();
        }
        return in.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
    }
}
