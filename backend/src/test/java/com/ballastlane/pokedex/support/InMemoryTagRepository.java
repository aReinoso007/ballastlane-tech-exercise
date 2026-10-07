package com.ballastlane.pokedex.support;

import com.ballastlane.pokedex.domain.port.TagRepository;
import java.util.ArrayList;
import java.util.List;

public class InMemoryTagRepository implements TagRepository {

    private final List<String> tags = new ArrayList<>();

    public InMemoryTagRepository with(String... names) {
        tags.addAll(List.of(names));
        return this;
    }

    @Override
    public List<String> findAll() {
        return List.copyOf(tags);
    }
}
