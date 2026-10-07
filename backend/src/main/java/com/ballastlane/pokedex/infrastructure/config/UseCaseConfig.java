package com.ballastlane.pokedex.infrastructure.config;

import com.ballastlane.pokedex.application.pokemon.DeleteLocalPokemon;
import com.ballastlane.pokedex.application.pokemon.GetLocalPokemon;
import com.ballastlane.pokedex.application.pokemon.GetPokemonDetail;
import com.ballastlane.pokedex.application.pokemon.ListLocalPokemon;
import com.ballastlane.pokedex.application.pokemon.ListPokemon;
import com.ballastlane.pokedex.application.pokemon.ListTags;
import com.ballastlane.pokedex.application.pokemon.SearchPokemon;
import com.ballastlane.pokedex.application.pokemon.SyncPokemon;
import com.ballastlane.pokedex.application.pokemon.UpdateLocalPokemon;
import com.ballastlane.pokedex.application.user.AuthenticateUser;
import com.ballastlane.pokedex.application.user.RegisterUser;
import com.ballastlane.pokedex.domain.port.PasswordHasher;
import com.ballastlane.pokedex.domain.port.PokemonCatalogPort;
import com.ballastlane.pokedex.domain.port.PokemonRepository;
import com.ballastlane.pokedex.domain.port.TagRepository;
import com.ballastlane.pokedex.domain.port.TokenIssuer;
import com.ballastlane.pokedex.domain.port.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Composition root: the application layer is plain Java, Spring only wires it here. */
@Configuration
public class UseCaseConfig {

    @Bean
    ListPokemon listPokemon(PokemonCatalogPort catalog) {
        return new ListPokemon(catalog);
    }

    @Bean
    SearchPokemon searchPokemon(PokemonCatalogPort catalog) {
        return new SearchPokemon(catalog);
    }

    @Bean
    GetPokemonDetail getPokemonDetail(PokemonCatalogPort catalog) {
        return new GetPokemonDetail(catalog);
    }

    @Bean
    SyncPokemon syncPokemon(PokemonCatalogPort catalog, PokemonRepository repository) {
        return new SyncPokemon(catalog, repository);
    }

    @Bean
    ListLocalPokemon listLocalPokemon(PokemonRepository repository) {
        return new ListLocalPokemon(repository);
    }

    @Bean
    GetLocalPokemon getLocalPokemon(PokemonRepository repository) {
        return new GetLocalPokemon(repository);
    }

    @Bean
    UpdateLocalPokemon updateLocalPokemon(PokemonRepository repository, TagRepository tags) {
        return new UpdateLocalPokemon(repository, tags);
    }

    @Bean
    ListTags listTags(TagRepository tags) {
        return new ListTags(tags);
    }

    @Bean
    DeleteLocalPokemon deleteLocalPokemon(PokemonRepository repository) {
        return new DeleteLocalPokemon(repository);
    }

    @Bean
    RegisterUser registerUser(UserRepository users, PasswordHasher hasher) {
        return new RegisterUser(users, hasher);
    }

    @Bean
    AuthenticateUser authenticateUser(UserRepository users, PasswordHasher hasher, TokenIssuer tokens) {
        return new AuthenticateUser(users, hasher, tokens);
    }
}
