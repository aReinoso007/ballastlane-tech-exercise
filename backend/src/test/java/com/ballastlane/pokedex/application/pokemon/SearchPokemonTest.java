package com.ballastlane.pokedex.application.pokemon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ballastlane.pokedex.domain.exception.InvalidDataException;
import com.ballastlane.pokedex.domain.model.PokemonName;
import com.ballastlane.pokedex.domain.port.PokemonCatalogPort;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SearchPokemonTest {

    private PokemonCatalogPort catalog;
    private SearchPokemon search;

    @BeforeEach
    void setUp() {
        catalog = mock(PokemonCatalogPort.class);
        when(catalog.listNames()).thenReturn(List.of(
                new PokemonName(25, "pikachu", "https://img/25.png"),
                new PokemonName(26, "raichu", "https://img/26.png")));
        search = new SearchPokemon(catalog);
    }

    @Test
    void returnsRankedMatchesFromTheCatalogIndex() {
        assertThat(search.execute("pikachuu", 5)).extracting(PokemonName::name).containsExactly("pikachu");
        verify(catalog).listNames();
    }

    @Test
    void blankQueryReturnsNothingWithoutCallingTheCatalog() {
        assertThat(search.execute("  ", 5)).isEmpty();
        verifyNoInteractions(catalog);
    }

    @Test
    void rejectsOverlongQueries() {
        assertThatThrownBy(() -> search.execute("a".repeat(SearchPokemon.MAX_QUERY_LENGTH + 1), 5))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("query");
        verifyNoInteractions(catalog);
    }

    @Test
    void rejectsLimitOutsideAllowedRange() {
        assertThatThrownBy(() -> search.execute("pika", 0)).isInstanceOf(InvalidDataException.class);
        assertThatThrownBy(() -> search.execute("pika", SearchPokemon.MAX_LIMIT + 1))
                .isInstanceOf(InvalidDataException.class);
        verifyNoInteractions(catalog);
    }
}
