package com.ballastlane.pokedex.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ballastlane.pokedex.support.Fixtures;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class LocalPokemonApiIT extends AbstractApiIT {

    private String auth;

    @BeforeEach
    void login() throws Exception {
        auth = bearerToken();
        when(catalog.fetch("bulbasaur")).thenReturn(Fixtures.bulbasaur());
        when(catalog.fetch("1")).thenReturn(Fixtures.bulbasaur());
    }

    private void sync() throws Exception {
        mvc.perform(post("/api/local/pokemon/bulbasaur/sync").header("Authorization", auth))
                .andExpect(status().isCreated());
    }

    @Test
    void syncPersistsPokemonAndReturnsCreated() throws Exception {
        mvc.perform(post("/api/local/pokemon/bulbasaur/sync").header("Authorization", auth))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/local/pokemon/1"))
                .andExpect(jsonPath("$.name").value("bulbasaur"))
                .andExpect(jsonPath("$.stats", hasSize(2)))
                .andExpect(jsonPath("$.evolutions", hasSize(2)));

        mvc.perform(get("/api/local/pokemon/1").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.abilities[1]").value("chlorophyll"));
    }

    @Test
    void syncingTwiceIs409() throws Exception {
        sync();
        mvc.perform(post("/api/local/pokemon/1/sync").header("Authorization", auth))
                .andExpect(status().isConflict());
    }

    @Test
    void syncRequiresAuthentication() throws Exception {
        mvc.perform(post("/api/local/pokemon/bulbasaur/sync")).andExpect(status().isUnauthorized());
    }

    @Test
    void listsPaginatedLocalPokemon() throws Exception {
        sync();
        mvc.perform(get("/api/local/pokemon").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    void missingPokemonIs404ForAllVerbs() throws Exception {
        mvc.perform(get("/api/local/pokemon/999").header("Authorization", auth)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/local/pokemon/999").header("Authorization", auth)).andExpect(status().isNotFound());
        mvc.perform(patch("/api/local/pokemon/999").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{\"region\":\"Kanto\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/local/pokemon/999").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content(body(validPut())))
                .andExpect(status().isNotFound());
    }

    @Test
    void nonNumericIdIs400() throws Exception {
        mvc.perform(get("/api/local/pokemon/abc").header("Authorization", auth)).andExpect(status().isBadRequest());
    }

    @Test
    void patchUpdatesCustomFieldsOnly() throws Exception {
        sync();
        mvc.perform(patch("/api/local/pokemon/1").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"region\":\"Kanto\",\"localizedName\":\"Bulbasaurio\",\"tags\":[\"starter\",\"grass\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.region").value("Kanto"))
                .andExpect(jsonPath("$.localizedName").value("Bulbasaurio"))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.name").value("bulbasaur"));

        mvc.perform(get("/api/local/pokemon/1").header("Authorization", auth))
                .andExpect(jsonPath("$.region").value("Kanto"));
    }

    @Test
    void putReplacesEditableFieldsAndClearsOmittedCustomOnes() throws Exception {
        sync();
        mvc.perform(patch("/api/local/pokemon/1").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{\"region\":\"Kanto\"}"));

        mvc.perform(put("/api/local/pokemon/1").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content(body(validPut())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("bulba"))
                .andExpect(jsonPath("$.weight").value(80))
                .andExpect(jsonPath("$.region").doesNotExist())
                .andExpect(jsonPath("$.stats", hasSize(2)));
    }

    @Test
    void putWithMissingMandatoryFieldsIs400() throws Exception {
        sync();
        mvc.perform(put("/api/local/pokemon/1").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void invalidValuesAreRejectedWith400() throws Exception {
        sync();
        for (String payload : List.of(
                "{\"weight\":-1}",
                "{\"height\":0}",
                "{\"name\":\"\"}",
                "{\"abilities\":[]}",
                "{\"abilities\":[\"\"]}",
                "{\"tags\":[\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\",\"11\"]}",
                "{\"region\":\"" + "x".repeat(51) + "\"}",
                "{\"weight\":\"heavy\"}",
                "{\"unknownField\":1}",
                "{}",
                "not json")) {
            mvc.perform(patch("/api/local/pokemon/1").header("Authorization", auth)
                            .contentType(MediaType.APPLICATION_JSON).content(payload))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void invalidPatchLeavesRecordUntouched() throws Exception {
        sync();
        mvc.perform(patch("/api/local/pokemon/1").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{\"weight\":-1,\"region\":\"Kanto\"}"));

        mvc.perform(get("/api/local/pokemon/1").header("Authorization", auth))
                .andExpect(jsonPath("$.weight").value(69))
                .andExpect(jsonPath("$.region").doesNotExist());
    }

    @Test
    void missingBodyIs400() throws Exception {
        sync();
        mvc.perform(patch("/api/local/pokemon/1").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void deleteRemovesPokemon() throws Exception {
        sync();
        mvc.perform(delete("/api/local/pokemon/1").header("Authorization", auth)).andExpect(status().isNoContent());
        mvc.perform(get("/api/local/pokemon/1").header("Authorization", auth)).andExpect(status().isNotFound());
        // can be synced again afterwards
        sync();
    }

    private static Map<String, Object> validPut() {
        return Map.of("name", "bulba", "height", 8, "weight", 80, "abilities", List.of("overgrow"));
    }

    @Test
    void newTagsJoinTheLibraryAndCanBeReusedIgnoringCase() throws Exception {
        sync();
        mvc.perform(get("/api/local/tags").header("Authorization", auth))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(patch("/api/local/pokemon/1").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"tags\":[\"Favourite\",\"sleepy\"]}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/local/tags").header("Authorization", auth))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value("Favourite"))
                .andExpect(jsonPath("$[1]").value("sleepy"));

        // typed again with another case: the stored spelling is reused and nothing duplicates
        mvc.perform(patch("/api/local/pokemon/1").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"tags\":[\"FAVOURITE\",\"new one\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[0]").value("Favourite"))
                .andExpect(jsonPath("$.tags[1]").value("new one"));
        mvc.perform(get("/api/local/tags").header("Authorization", auth))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void tagsStayInTheLibraryAfterThePokemonIsDeleted() throws Exception {
        sync();
        mvc.perform(patch("/api/local/pokemon/1").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{\"tags\":[\"keeper\"]}")).andExpect(status().isOk());
        mvc.perform(delete("/api/local/pokemon/1").header("Authorization", auth)).andExpect(status().isNoContent());

        mvc.perform(get("/api/local/tags").header("Authorization", auth))
                .andExpect(jsonPath("$[0]").value("keeper"));
    }

    @Test
    void tagLibraryRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/local/tags")).andExpect(status().isUnauthorized());
    }
}
