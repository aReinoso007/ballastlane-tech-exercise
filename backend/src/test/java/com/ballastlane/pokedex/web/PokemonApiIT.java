package com.ballastlane.pokedex.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ballastlane.pokedex.domain.exception.CatalogUnavailableException;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonName;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import com.ballastlane.pokedex.support.Fixtures;
import java.util.List;
import org.junit.jupiter.api.Test;

class PokemonApiIT extends AbstractApiIT {

    @Test
    void listsPokemonPublicly() throws Exception {
        when(catalog.list(1, 2)).thenReturn(new PageResult<>(
                List.of(Fixtures.summaryOf(Fixtures.bulbasaur()), Fixtures.summaryOf(Fixtures.pikachu())), 1, 2, 1302));

        mvc.perform(get("/api/pokemon").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].name").value("bulbasaur"))
                .andExpect(jsonPath("$.items[0].spriteUrl").value("https://img/1.png"))
                .andExpect(jsonPath("$.items[0].category").value("Seed Pokémon"))
                .andExpect(jsonPath("$.items[0].weight").value(69))
                .andExpect(jsonPath("$.items[0].abilities[0]").value("overgrow"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalItems").value(1302))
                .andExpect(jsonPath("$.totalPages").value(651));
    }

    @Test
    void suggestsPokemonDespiteTyposPublicly() throws Exception {
        when(catalog.listNames()).thenReturn(List.of(
                new PokemonName(25, "pikachu", "https://img/25.png"),
                new PokemonName(26, "raichu", "https://img/26.png")));

        mvc.perform(get("/api/pokemon/search").param("q", "pikachuu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(25))
                .andExpect(jsonPath("$[0].name").value("pikachu"))
                .andExpect(jsonPath("$[0].spriteUrl").value("https://img/25.png"));
    }

    @Test
    void searchWithoutQueryReturnsEmptyList() throws Exception {
        mvc.perform(get("/api/pokemon/search")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void searchRejectsBadLimitAndOverlongQuery() throws Exception {
        mvc.perform(get("/api/pokemon/search").param("q", "pika").param("limit", "999"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/pokemon/search").param("q", "a".repeat(60))).andExpect(status().isBadRequest());
    }

    @Test
    void searchReportsBadGatewayWhenCatalogIsDown() throws Exception {
        when(catalog.listNames()).thenThrow(new CatalogUnavailableException("down", null));

        mvc.perform(get("/api/pokemon/search").param("q", "pika")).andExpect(status().isBadGateway());
    }

    @Test
    void usesDefaultPaging() throws Exception {
        when(catalog.list(0, 20)).thenReturn(new PageResult<PokemonSummary>(List.of(), 0, 20, 0));

        mvc.perform(get("/api/pokemon")).andExpect(status().isOk()).andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void rejectsOutOfRangePagingWith400() throws Exception {
        mvc.perform(get("/api/pokemon").param("size", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("size must be between 1 and 50"));
        mvc.perform(get("/api/pokemon").param("page", "-3")).andExpect(status().isBadRequest());
    }

    @Test
    void rejectsNonNumericPagingWith400() throws Exception {
        mvc.perform(get("/api/pokemon").param("page", "abc")).andExpect(status().isBadRequest());
    }

    @Test
    void returnsFullDetail() throws Exception {
        when(catalog.fetch("bulbasaur")).thenReturn(Fixtures.bulbasaur());

        mvc.perform(get("/api/pokemon/Bulbasaur"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.spriteUrl").value("https://img/1.png"))
                .andExpect(jsonPath("$.description").value("A strange seed was planted on its back at birth."))
                .andExpect(jsonPath("$.stats[0].name").value("hp"))
                .andExpect(jsonPath("$.stats[0].baseStat").value(45))
                .andExpect(jsonPath("$.evolutions", hasSize(2)))
                .andExpect(jsonPath("$.evolutions[1].evolvesFrom").value("bulbasaur"));
    }

    @Test
    void unknownPokemonIs404() throws Exception {
        when(catalog.fetch(anyString())).thenThrow(new PokemonNotFoundException("nope"));

        mvc.perform(get("/api/pokemon/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not found"));
    }

    @Test
    void upstreamFailureIs502WithoutLeakingInternals() throws Exception {
        when(catalog.fetch(anyString())).thenThrow(new CatalogUnavailableException("boom internal detail", null));

        mvc.perform(get("/api/pokemon/1"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("internal"))));
    }
}
