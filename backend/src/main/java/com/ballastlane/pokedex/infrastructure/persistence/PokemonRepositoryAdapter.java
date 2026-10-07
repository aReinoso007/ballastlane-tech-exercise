package com.ballastlane.pokedex.infrastructure.persistence;

import com.ballastlane.pokedex.domain.model.EvolutionStage;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.domain.model.Stat;
import com.ballastlane.pokedex.domain.port.PokemonRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PokemonRepositoryAdapter implements PokemonRepository {

    private final SpringDataPokemonRepository jpa;

    public PokemonRepositoryAdapter(SpringDataPokemonRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Pokemon> findAll(int page, int size) {
        Page<PokemonEntity> result = jpa.findAll(PageRequest.of(page, size, Sort.by("id")));
        return new PageResult<>(result.getContent().stream().map(PokemonRepositoryAdapter::toDomain).toList(),
                page, size, result.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pokemon> findById(int id) {
        return jpa.findById(id).map(PokemonRepositoryAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(int id) {
        return jpa.existsById(id);
    }

    @Override
    public Pokemon save(Pokemon pokemon) {
        PokemonEntity entity = jpa.findById(pokemon.id()).orElseGet(() -> new PokemonEntity(pokemon.id()));
        copyInto(entity, pokemon);
        return toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    public boolean deleteById(int id) {
        if (!jpa.existsById(id)) {
            return false;
        }
        jpa.deleteById(id);
        jpa.flush();
        return true;
    }

    private static void copyInto(PokemonEntity e, Pokemon p) {
        e.setName(p.name());
        e.setHeight(p.height());
        e.setWeight(p.weight());
        e.setSpriteUrl(p.spriteUrl());
        e.setCategory(p.category());
        e.setDescription(p.description());
        e.setLocalizedName(p.localizedName());
        e.setRegion(p.region());
        replace(e.getAbilities(), p.abilities());
        replace(e.getTags(), p.tags());
        replace(e.getStats(), p.stats().stream()
                .map(s -> new PokemonEntity.StatEmbeddable(s.name(), s.baseStat())).toList());
        replace(e.getEvolutions(), p.evolutions().stream()
                .map(s -> new PokemonEntity.EvolutionEmbeddable(s.id(), s.name(), s.spriteUrl(), s.stage(),
                        s.evolvesFrom())).toList());
    }

    private static <T> void replace(java.util.List<T> target, java.util.List<T> source) {
        target.clear();
        target.addAll(source);
    }

    private static Pokemon toDomain(PokemonEntity e) {
        return new Pokemon(
                e.getId(),
                e.getName(),
                e.getHeight(),
                e.getWeight(),
                e.getSpriteUrl(),
                e.getCategory(),
                e.getDescription(),
                e.getAbilities(),
                e.getStats().stream().map(s -> new Stat(s.getName(), s.getBaseStat())).toList(),
                e.getEvolutions().stream()
                        .map(s -> new EvolutionStage(s.getEvolutionId(), s.getName(), s.getSpriteUrl(), s.getStage(),
                                s.getEvolvesFrom())).toList(),
                e.getLocalizedName(),
                e.getRegion(),
                e.getTags());
    }
}
