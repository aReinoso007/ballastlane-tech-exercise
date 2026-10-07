package com.ballastlane.pokedex.domain.model;

import java.util.List;

/**
 * Editable subset of a {@link Pokemon}. Every field is optional so the same type can express both a
 * partial update (PATCH) and a full replacement (PUT).
 */
public record PokemonEdit(
        String name,
        Integer height,
        Integer weight,
        String category,
        String description,
        List<String> abilities,
        String localizedName,
        String region,
        List<String> tags) {

    public boolean isEmpty() {
        return name == null && height == null && weight == null && category == null && description == null
                && abilities == null && localizedName == null && region == null && tags == null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String name;
        private Integer height;
        private Integer weight;
        private String category;
        private String description;
        private List<String> abilities;
        private String localizedName;
        private String region;
        private List<String> tags;

        public Builder name(String v) {
            this.name = v;
            return this;
        }

        public Builder height(Integer v) {
            this.height = v;
            return this;
        }

        public Builder weight(Integer v) {
            this.weight = v;
            return this;
        }

        public Builder category(String v) {
            this.category = v;
            return this;
        }

        public Builder description(String v) {
            this.description = v;
            return this;
        }

        public Builder abilities(List<String> v) {
            this.abilities = v;
            return this;
        }

        public Builder localizedName(String v) {
            this.localizedName = v;
            return this;
        }

        public Builder region(String v) {
            this.region = v;
            return this;
        }

        public Builder tags(List<String> v) {
            this.tags = v;
            return this;
        }

        public PokemonEdit build() {
            return new PokemonEdit(name, height, weight, category, description, abilities, localizedName, region,
                    tags);
        }
    }
}
