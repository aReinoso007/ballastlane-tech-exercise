package com.ballastlane.pokedex.support;

import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.domain.port.PokemonRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class InMemoryPokemonRepository implements PokemonRepository {

    private final Map<Integer, Pokemon> store = new TreeMap<>();
    public int saveCalls = 0;

    @Override
    public PageResult<Pokemon> findAll(int page, int size) {
        List<Pokemon> all = new ArrayList<>(store.values());
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        return new PageResult<>(all.subList(from, to), page, size, all.size());
    }

    @Override
    public Optional<Pokemon> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean existsById(int id) {
        return store.containsKey(id);
    }

    @Override
    public Pokemon save(Pokemon pokemon) {
        saveCalls++;
        store.put(pokemon.id(), pokemon);
        return pokemon;
    }

    @Override
    public boolean deleteById(int id) {
        return store.remove(id) != null;
    }
}
