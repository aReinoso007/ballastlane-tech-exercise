package com.ballastlane.pokedex.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pokemon")
public class PokemonEntity {

    @Id
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int height;

    @Column(nullable = false)
    private int weight;

    @Column(name = "sprite_url")
    private String spriteUrl;

    private String category;

    private String description;

    @Column(name = "localized_name")
    private String localizedName;

    private String region;

    @ElementCollection
    @CollectionTable(name = "pokemon_ability", joinColumns = @JoinColumn(name = "pokemon_id"))
    @OrderColumn(name = "position")
    @Column(name = "name", nullable = false)
    private List<String> abilities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "pokemon_stat", joinColumns = @JoinColumn(name = "pokemon_id"))
    @OrderColumn(name = "position")
    private List<StatEmbeddable> stats = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "pokemon_tag", joinColumns = @JoinColumn(name = "pokemon_id"))
    @OrderColumn(name = "position")
    @Column(name = "tag", nullable = false)
    private List<String> tags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "pokemon_evolution", joinColumns = @JoinColumn(name = "pokemon_id"))
    @OrderColumn(name = "position")
    private List<EvolutionEmbeddable> evolutions = new ArrayList<>();

    protected PokemonEntity() {
    }

    public PokemonEntity(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getSpriteUrl() {
        return spriteUrl;
    }

    public void setSpriteUrl(String spriteUrl) {
        this.spriteUrl = spriteUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public void setLocalizedName(String localizedName) {
        this.localizedName = localizedName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<String> getAbilities() {
        return abilities;
    }

    public List<StatEmbeddable> getStats() {
        return stats;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<EvolutionEmbeddable> getEvolutions() {
        return evolutions;
    }

    @Embeddable
    public static class StatEmbeddable {

        @Column(name = "name", nullable = false)
        private String name;

        @Column(name = "base_stat", nullable = false)
        private int baseStat;

        protected StatEmbeddable() {
        }

        public StatEmbeddable(String name, int baseStat) {
            this.name = name;
            this.baseStat = baseStat;
        }

        public String getName() {
            return name;
        }

        public int getBaseStat() {
            return baseStat;
        }
    }

    @Embeddable
    public static class EvolutionEmbeddable {

        @Column(name = "evolution_id", nullable = false)
        private int evolutionId;

        @Column(name = "name", nullable = false)
        private String name;

        @Column(name = "sprite_url")
        private String spriteUrl;

        @Column(name = "stage", nullable = false)
        private int stage;

        @Column(name = "evolves_from")
        private String evolvesFrom;

        protected EvolutionEmbeddable() {
        }

        public EvolutionEmbeddable(int evolutionId, String name, String spriteUrl, int stage, String evolvesFrom) {
            this.evolutionId = evolutionId;
            this.name = name;
            this.spriteUrl = spriteUrl;
            this.stage = stage;
            this.evolvesFrom = evolvesFrom;
        }

        public int getEvolutionId() {
            return evolutionId;
        }

        public String getName() {
            return name;
        }

        public String getSpriteUrl() {
            return spriteUrl;
        }

        public int getStage() {
            return stage;
        }

        public String getEvolvesFrom() {
            return evolvesFrom;
        }
    }
}
