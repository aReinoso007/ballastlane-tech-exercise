package com.ballastlane.pokedex.web;

import com.ballastlane.pokedex.application.pokemon.ListTags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/local/tags")
@Tag(name = "Tags", description = "Tags used on local Pokemon, offered for reuse (JWT required)")
@SecurityRequirement(name = "bearerAuth")
public class TagController {

    private final ListTags listTags;

    public TagController(ListTags listTags) {
        this.listTags = listTags;
    }

    @GetMapping
    @Operation(summary = "Tags used before, alphabetically; new tags join the list when a Pokemon is saved with them")
    public List<String> list() {
        return listTags.execute();
    }
}
