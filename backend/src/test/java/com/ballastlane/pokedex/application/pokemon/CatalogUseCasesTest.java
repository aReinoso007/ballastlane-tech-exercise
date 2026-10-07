package com.ballastlane.pokedex.application.pokemon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ballastlane.pokedex.domain.exception.InvalidDataException;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import com.ballastlane.pokedex.domain.port.PokemonCatalogPort;
import com.ballastlane.pokedex.support.Fixtures;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CatalogUseCasesTest {

    private PokemonCatalogPort catalog;

    @BeforeEach
    void setUp() {
        catalog = mock(PokemonCatalogPort.class);
    }

    @Nested
    class ListPokemonTest {

        @Test
        void delegatesToCatalog() {
            PageResult<PokemonSummary> page =
                    new PageResult<>(List.of(Fixtures.summaryOf(Fixtures.bulbasaur())), 2, 20, 100);
            when(catalog.list(2, 20)).thenReturn(page);

            assertThat(new ListPokemon(catalog).execute(2, 20)).isSameAs(page);
        }

        @Test
        void rejectsNegativePage() {
            assertThatThrownBy(() -> new ListPokemon(catalog).execute(-1, 20))
                    .isInstanceOf(InvalidDataException.class)
                    .hasMessageContaining("page");
            verifyNoInteractions(catalog);
        }

        @Test
        void rejectsSizeOutsideAllowedRange() {
            ListPokemon useCase = new ListPokemon(catalog);
            assertThatThrownBy(() -> useCase.execute(0, 0)).isInstanceOf(InvalidDataException.class);
            assertThatThrownBy(() -> useCase.execute(0, 51)).isInstanceOf(InvalidDataException.class);
            verifyNoInteractions(catalog);
        }
    }

    @Nested
    class GetPokemonDetailTest {

        @Test
        void normalizesIdentifierBeforeQuerying() {
            when(catalog.fetch("pikachu")).thenReturn(Fixtures.pikachu());

            assertThat(new GetPokemonDetail(catalog).execute("  Pikachu ")).isEqualTo(Fixtures.pikachu());
            verify(catalog).fetch("pikachu");
        }

        @Test
        void rejectsBlankIdentifier() {
            assertThatThrownBy(() -> new GetPokemonDetail(catalog).execute("  "))
                    .isInstanceOf(InvalidDataException.class);
            verifyNoInteractions(catalog);
        }

        @Test
        void propagatesNotFound() {
            when(catalog.fetch("nope")).thenThrow(new PokemonNotFoundException("nope"));
            assertThatThrownBy(() -> new GetPokemonDetail(catalog).execute("nope"))
                    .isInstanceOf(PokemonNotFoundException.class);
        }
    }
}
