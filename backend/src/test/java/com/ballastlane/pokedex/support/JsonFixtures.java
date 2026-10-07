package com.ballastlane.pokedex.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public final class JsonFixtures {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonFixtures() {
    }

    public static String raw(String name) {
        try (InputStream in = JsonFixtures.class.getResourceAsStream("/pokeapi/" + name + ".json")) {
            if (in == null) {
                throw new IllegalArgumentException("Missing fixture " + name);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static JsonNode node(String name) {
        try {
            return MAPPER.readTree(raw(name));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
