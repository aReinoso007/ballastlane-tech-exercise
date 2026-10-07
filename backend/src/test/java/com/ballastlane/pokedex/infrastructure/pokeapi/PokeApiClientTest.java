package com.ballastlane.pokedex.infrastructure.pokeapi;

import static com.ballastlane.pokedex.support.JsonFixtures.raw;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.ballastlane.pokedex.domain.exception.CatalogUnavailableException;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class PokeApiClientTest {

    private static final String BASE = "https://pokeapi.test/api/v2";

    @Nested
    class Behaviour {

        private MockRestServiceServer server;
        private PokeApiClient client;

        @BeforeEach
        void setUp() {
            RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
            server = MockRestServiceServer.bindTo(builder).build();
            client = new PokeApiClient(builder.build());
        }

        @Test
        void fetchesPokemonJson() {
            server.expect(once(), requestTo(BASE + "/pokemon/bulbasaur"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(raw("pokemon-bulbasaur"), MediaType.APPLICATION_JSON));

            JsonNode json = client.pokemon("bulbasaur");

            assertThat(json.path("name").asText()).isEqualTo("bulbasaur");
            server.verify();
        }

        @Test
        void requestsPageWithLimitAndOffset() {
            server.expect(once(), requestTo(BASE + "/pokemon?limit=2&offset=4"))
                    .andRespond(withSuccess(raw("page-limit2"), MediaType.APPLICATION_JSON));

            assertThat(client.page(2, 4).path("count").asInt()).isEqualTo(1302);
            server.verify();
        }

        @Test
        void requestsTheWholeNameIndexInOneCall() {
            server.expect(once(), requestTo(BASE + "/pokemon?limit=100000&offset=0"))
                    .andRespond(withSuccess(raw("names-list"), MediaType.APPLICATION_JSON));

            assertThat(client.names().path("results")).hasSize(4);
            server.verify();
        }

        @Test
        void mapsHttp404ToNotFound() {
            server.expect(once(), requestTo(BASE + "/pokemon/nope")).andRespond(withStatus(HttpStatus.NOT_FOUND));

            assertThatThrownBy(() -> client.pokemon("nope")).isInstanceOf(PokemonNotFoundException.class);
        }

        @Test
        void mapsServerErrorToCatalogUnavailable() {
            server.expect(once(), requestTo(BASE + "/pokemon/1")).andRespond(withServerError());

            assertThatThrownBy(() -> client.pokemon("1")).isInstanceOf(CatalogUnavailableException.class);
        }

        @Test
        void mapsMalformedBodyToCatalogUnavailable() {
            server.expect(once(), requestTo(BASE + "/pokemon/1"))
                    .andRespond(withSuccess("not json at all", MediaType.APPLICATION_JSON));

            assertThatThrownBy(() -> client.pokemon("1")).isInstanceOf(CatalogUnavailableException.class);
        }
    }

    @Nested
    @SpringJUnitConfig(CachingConfig.class)
    class Caching {

        @org.springframework.beans.factory.annotation.Autowired
        PokeApiClient client;

        @org.springframework.beans.factory.annotation.Autowired
        MockRestServiceServer server;

        @Test
        void secondCallIsServedFromCache() {
            server.expect(once(), requestTo(BASE + "/pokemon/pikachu"))
                    .andRespond(withSuccess(raw("pokemon-bulbasaur"), MediaType.APPLICATION_JSON));

            JsonNode first = client.pokemon("pikachu");
            JsonNode second = client.pokemon("pikachu");

            assertThat(second).isSameAs(first);
            server.verify();
        }
    }

    @TestConfiguration
    @EnableCaching
    static class CachingConfig {

        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder().baseUrl(BASE);
        }

        @Bean
        MockRestServiceServer mockServer(RestClient.Builder builder) {
            return MockRestServiceServer.bindTo(builder).build();
        }

        @Bean
        CacheManager cacheManager() {
            return new CaffeineCacheManager("pokeapi-page", "pokeapi-pokemon", "pokeapi-species", "pokeapi-evolution");
        }

        @Bean
        PokeApiClient pokeApiClient(RestClient.Builder builder, MockRestServiceServer ignoredToEnsureBinding) {
            return new PokeApiClient(builder.build());
        }
    }
}
