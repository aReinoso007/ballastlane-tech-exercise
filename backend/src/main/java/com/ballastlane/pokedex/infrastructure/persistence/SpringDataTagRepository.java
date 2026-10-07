package com.ballastlane.pokedex.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataTagRepository extends JpaRepository<TagEntity, Long> {

    @Query("select t.name from TagEntity t order by lower(t.name), t.name")
    List<String> findAllNames();

    /** Adds the tag unless one with the same name (ignoring case) exists; safe under concurrent saves. */
    @Modifying
    @Query(value = "insert into tag (name) values (:name) on conflict do nothing", nativeQuery = true)
    void insertIfAbsent(@Param("name") String name);
}
