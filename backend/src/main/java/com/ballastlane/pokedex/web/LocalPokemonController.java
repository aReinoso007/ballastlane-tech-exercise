package com.ballastlane.pokedex.web;

import com.ballastlane.pokedex.application.pokemon.DeleteLocalPokemon;
import com.ballastlane.pokedex.application.pokemon.GetLocalPokemon;
import com.ballastlane.pokedex.application.pokemon.ListLocalPokemon;
import com.ballastlane.pokedex.application.pokemon.SyncPokemon;
import com.ballastlane.pokedex.application.pokemon.UpdateLocalPokemon;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.web.dto.PageResponse;
import com.ballastlane.pokedex.web.dto.PokemonPatchRequest;
import com.ballastlane.pokedex.web.dto.PokemonResponse;
import com.ballastlane.pokedex.web.dto.PokemonUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/local/pokemon")
@Tag(name = "Local Pokemon", description = "Pokemon replicated into the local database (JWT required)")
@SecurityRequirement(name = "bearerAuth")
public class LocalPokemonController {

    private final SyncPokemon syncPokemon;
    private final ListLocalPokemon listLocalPokemon;
    private final GetLocalPokemon getLocalPokemon;
    private final UpdateLocalPokemon updateLocalPokemon;
    private final DeleteLocalPokemon deleteLocalPokemon;

    public LocalPokemonController(SyncPokemon syncPokemon, ListLocalPokemon listLocalPokemon,
                                  GetLocalPokemon getLocalPokemon, UpdateLocalPokemon updateLocalPokemon,
                                  DeleteLocalPokemon deleteLocalPokemon) {
        this.syncPokemon = syncPokemon;
        this.listLocalPokemon = listLocalPokemon;
        this.getLocalPokemon = getLocalPokemon;
        this.updateLocalPokemon = updateLocalPokemon;
        this.deleteLocalPokemon = deleteLocalPokemon;
    }

    @PostMapping("/{idOrName}/sync")
    @Operation(summary = "US03 - Copy a Pokemon from PokeAPI into the local database")
    public ResponseEntity<PokemonResponse> sync(@PathVariable String idOrName) {
        Pokemon saved = syncPokemon.execute(idOrName);
        return ResponseEntity.created(URI.create("/api/local/pokemon/" + saved.id()))
                .body(PokemonResponse.from(saved));
    }

    @GetMapping
    @Operation(summary = "List locally stored Pokemon")
    public PageResponse<PokemonResponse> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(listLocalPokemon.execute(page, size), PokemonResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a locally stored Pokemon")
    public PokemonResponse get(@PathVariable int id) {
        return PokemonResponse.from(getLocalPokemon.execute(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "US04 - Replace all editable fields")
    public PokemonResponse replace(@PathVariable int id, @Valid @RequestBody PokemonUpdateRequest request) {
        return PokemonResponse.from(updateLocalPokemon.replace(id, request.toEdit()));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "US04 - Partially update editable fields")
    public PokemonResponse patch(@PathVariable int id, @Valid @RequestBody PokemonPatchRequest request) {
        return PokemonResponse.from(updateLocalPokemon.patch(id, request.toEdit()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a Pokemon from the local database")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        deleteLocalPokemon.execute(id);
        return ResponseEntity.noContent().build();
    }
}
