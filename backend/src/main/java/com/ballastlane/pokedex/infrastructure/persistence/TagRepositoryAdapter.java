package com.ballastlane.pokedex.infrastructure.persistence;

import com.ballastlane.pokedex.domain.port.TagRepository;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class TagRepositoryAdapter implements TagRepository {

    private final SpringDataTagRepository jpa;

    public TagRepositoryAdapter(SpringDataTagRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<String> findAll() {
        return jpa.findAllNames();
    }
}
