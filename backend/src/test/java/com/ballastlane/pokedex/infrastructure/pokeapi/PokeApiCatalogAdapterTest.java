package com.ballastlane.pokedex.infrastructure.pokeapi;

import static com.ballastlane.pokedex.support.JsonFixtures.node;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ballastlane.pokedex.domain.exception.CatalogUnavailableException;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.PageResult;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.domain.model.PokemonSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PokeApiCatalogAdapterTest {

    private PokeApiClient client;
    private PokeApiCatalogAdapter adapter;

    @BeforeEach
    void setUp() {
        client = mock(PokeApiClient.class);
        adapter = new PokeApiCatalogAdapter(client, Runnable::run);
    }

    @Test
    void listsPageKeepingPokeApiOrder() {
        when(client.page(2, 0)).thenReturn(node("page-limit2"));
        when(client.pokemon("bulbasaur")).thenReturn(node("pokemon-bulbasaur"));
        when(client.species("bulbasaur")).thenReturn(node("species-bulbasaur"));
        when(client.pokemon("ivysaur")).thenReturn(node("pokemon-ivysaur"));
        when(client.species("ivysaur")).thenReturn(node("species-ivysaur"));

        PageResult<PokemonSummary> page = adapter.list(0, 2);

        assertThat(page.items()).extracting(PokemonSummary::name).containsExactly("bulbasaur", "ivysaur");
        assertThat(page.totalItems()).isEqualTo(1302);
        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(2);
        assertThat(page.items().get(0).category()).isEqualTo("Seed Pokémon");
    }

    @Test
    void usesOffsetFromPageAndSize() {
        when(client.page(2, 6)).thenReturn(new ObjectMapper().createObjectNode().put("count", 10));

        PageResult<PokemonSummary> page = adapter.list(3, 2);

        assertThat(page.items()).isEmpty();
        assertThat(page.totalItems()).isEqualTo(10);
    }

    @Test
    void fetchesDetailCombiningPokemonSpeciesAndEvolutionChain() {
        when(client.pokemon("bulbasaur")).thenReturn(node("pokemon-bulbasaur"));
        when(client.species("bulbasaur")).thenReturn(node("species-bulbasaur"));
        when(client.evolutionChain(1)).thenReturn(node("evolution-chain-1"));

        Pokemon p = adapter.fetch("bulbasaur");

        assertThat(p.id()).isEqualTo(1);
        assertThat(p.category()).isEqualTo("Seed Pokémon");
        assertThat(p.evolutions()).hasSize(3);
    }

    @Test
    void skipsEvolutionChainWhenSpeciesHasNone() throws Exception {
        when(client.pokemon("bulbasaur")).thenReturn(node("pokemon-bulbasaur"));
        when(client.species("bulbasaur")).thenReturn(new ObjectMapper().readTree(
                "{\"genera\":[],\"flavor_text_entries\":[],\"evolution_chain\":null}"));

        Pokemon p = adapter.fetch("bulbasaur");

        assertThat(p.evolutions()).isEmpty();
        verify(client, never()).evolutionChain(1);
    }

    @Test
    void propagatesNotFound() {
        when(client.pokemon("zzz")).thenThrow(new PokemonNotFoundException("zzz"));
        assertThatThrownBy(() -> adapter.fetch("zzz")).isInstanceOf(PokemonNotFoundException.class);
    }

    @Test
    void propagatesCatalogFailuresThroughAsyncListing() {
        when(client.page(2, 0)).thenReturn(node("page-limit2"));
        when(client.pokemon("bulbasaur")).thenThrow(new CatalogUnavailableException("down", null));

        assertThatThrownBy(() -> adapter.list(0, 2)).isInstanceOf(CatalogUnavailableException.class);
    }

    @Test
    void treatsUnmappableRemoteDataAsCatalogProblem() throws Exception {
        when(client.pokemon("bad")).thenReturn(new ObjectMapper().readTree(
                "{\"id\":5,\"name\":\"bad\",\"height\":0,\"weight\":0,\"abilities\":[],\"stats\":[],\"species\":{\"name\":\"bad\"},\"sprites\":{}}"));
        when(client.species("bad")).thenReturn(new ObjectMapper().readTree("{}"));

        assertThatThrownBy(() -> adapter.fetch("bad")).isInstanceOf(CatalogUnavailableException.class);
    }
}
