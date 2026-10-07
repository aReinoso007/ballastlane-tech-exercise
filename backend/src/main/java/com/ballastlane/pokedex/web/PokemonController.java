package com.ballastlane.pokedex.web;

import com.ballastlane.pokedex.application.pokemon.GetPokemonDetail;
import com.ballastlane.pokedex.application.pokemon.ListPokemon;
import com.ballastlane.pokedex.web.dto.PageResponse;
import com.ballastlane.pokedex.web.dto.PokemonResponse;
import com.ballastlane.pokedex.web.dto.PokemonSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pokemon")
@Tag(name = "Pokemon catalog", description = "Read-only view of PokeAPI data (public)")
public class PokemonController {

    private final ListPokemon listPokemon;
    private final GetPokemonDetail getPokemonDetail;

    public PokemonController(ListPokemon listPokemon, GetPokemonDetail getPokemonDetail) {
        this.listPokemon = listPokemon;
        this.getPokemonDetail = getPokemonDetail;
    }

    @GetMapping
    @Operation(summary = "US01 - Paginated list with sprite, category, weight and abilities")
    public PageResponse<PokemonSummaryResponse> list(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(listPokemon.execute(page, size), PokemonSummaryResponse::from);
    }

    @GetMapping("/{idOrName}")
    @Operation(summary = "US02 - Detail with image, stats, description and evolution lineage")
    public PokemonResponse detail(@PathVariable String idOrName) {
        return PokemonResponse.from(getPokemonDetail.execute(idOrName));
    }
}
