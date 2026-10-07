package com.ballastlane.pokedex.infrastructure.pokeapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pokeapi")
public record PokeApiProperties(String baseUrl, int timeoutSeconds) {

    public PokeApiProperties {
        if (timeoutSeconds <= 0) {
            timeoutSeconds = 10;
        }
    }
}
