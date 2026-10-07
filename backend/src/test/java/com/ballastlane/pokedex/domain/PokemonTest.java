package com.ballastlane.pokedex.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ballastlane.pokedex.domain.exception.InvalidDataException;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.domain.model.PokemonEdit;
import com.ballastlane.pokedex.domain.model.Stat;
import com.ballastlane.pokedex.support.Fixtures;
import java.util.List;
import org.junit.jupiter.api.Test;

class PokemonTest {

    @Test
    void createsValidPokemon() {
        Pokemon p = Fixtures.bulbasaur();
        assertThat(p.name()).isEqualTo("bulbasaur");
        assertThat(p.abilities()).containsExactly("overgrow", "chlorophyll");
    }

    @Test
    void trimsTextFields() {
        Pokemon base = Fixtures.bulbasaur();
        Pokemon p = new Pokemon(base.id(), "  bulbasaur ", 7, 69, null, " Seed ", null,
                List.of(" overgrow "), List.of(), List.of(), " Bulbasaurio ", " Kanto ", List.of(" starter "));
        assertThat(p.name()).isEqualTo("bulbasaur");
        assertThat(p.category()).isEqualTo("Seed");
        assertThat(p.abilities()).containsExactly("overgrow");
        assertThat(p.localizedName()).isEqualTo("Bulbasaurio");
        assertThat(p.region()).isEqualTo("Kanto");
        assertThat(p.tags()).containsExactly("starter");
    }

    @Test
    void rejectsBlankNameAndNonPositiveMeasurements() {
        assertThatThrownBy(() -> new Pokemon(1, " ", 0, -1, null, null, null,
                List.of("overgrow"), List.of(), List.of(), null, null, List.of()))
                .isInstanceOf(InvalidDataException.class)
                .satisfies(e -> assertThat(((InvalidDataException) e).violations())
                        .hasSize(3)
                        .anyMatch(v -> v.contains("name"))
                        .anyMatch(v -> v.contains("height"))
                        .anyMatch(v -> v.contains("weight")));
    }

    @Test
    void rejectsEmptyAbilities() {
        assertThatThrownBy(() -> new Pokemon(1, "a", 1, 1, null, null, null,
                List.of(), List.of(), List.of(), null, null, List.of()))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("abilities");
    }

    @Test
    void rejectsStatOutOfRange() {
        assertThatThrownBy(() -> new Pokemon(1, "a", 1, 1, null, null, null,
                List.of("x"), List.of(new Stat("hp", 999)), List.of(), null, null, List.of()))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("stat");
    }

    @Test
    void rejectsTooManyOrTooLongTags() {
        List<String> tooMany = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
        Pokemon base = Fixtures.bulbasaur();
        assertThatThrownBy(() -> base.applyPartial(PokemonEdit.builder().tags(tooMany).build()))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("tags");
        assertThatThrownBy(() -> base.applyPartial(PokemonEdit.builder().tags(List.of("x".repeat(31))).build()))
                .isInstanceOf(InvalidDataException.class);
    }

    @Test
    void applyPartialOnlyChangesProvidedFields() {
        Pokemon base = Fixtures.bulbasaur();
        Pokemon updated = base.applyPartial(PokemonEdit.builder().region("Kanto").weight(70).build());

        assertThat(updated.region()).isEqualTo("Kanto");
        assertThat(updated.weight()).isEqualTo(70);
        assertThat(updated.name()).isEqualTo(base.name());
        assertThat(updated.abilities()).isEqualTo(base.abilities());
        assertThat(updated.stats()).isEqualTo(base.stats());
        assertThat(updated.evolutions()).isEqualTo(base.evolutions());
    }

    @Test
    void applyPartialRejectsEmptyEdit() {
        assertThatThrownBy(() -> Fixtures.bulbasaur().applyPartial(PokemonEdit.builder().build()))
                .isInstanceOf(InvalidDataException.class)
                .hasMessageContaining("at least one");
    }

    @Test
    void replaceOverwritesAllEditableFieldsAndClearsMissingCustomOnes() {
        Pokemon base = Fixtures.bulbasaur()
                .applyPartial(PokemonEdit.builder().region("Kanto").localizedName("Bulbi").tags(List.of("a")).build());

        Pokemon replaced = base.replaceWith(PokemonEdit.builder()
                .name("bulba").height(8).weight(80).abilities(List.of("overgrow")).build());

        assertThat(replaced.name()).isEqualTo("bulba");
        assertThat(replaced.height()).isEqualTo(8);
        assertThat(replaced.region()).isNull();
        assertThat(replaced.localizedName()).isNull();
        assertThat(replaced.tags()).isEmpty();
        assertThat(replaced.id()).isEqualTo(1);
        assertThat(replaced.stats()).isEqualTo(base.stats());
    }

    @Test
    void replaceRequiresMandatoryFields() {
        assertThatThrownBy(() -> Fixtures.bulbasaur().replaceWith(PokemonEdit.builder().name("x").build()))
                .isInstanceOf(InvalidDataException.class)
                .satisfies(e -> assertThat(((InvalidDataException) e).violations())
                        .anyMatch(v -> v.contains("height"))
                        .anyMatch(v -> v.contains("weight"))
                        .anyMatch(v -> v.contains("abilities")));
    }

    @Test
    void listsAreImmutable() {
        Pokemon p = Fixtures.bulbasaur();
        assertThatThrownBy(() -> p.abilities().add("x")).isInstanceOf(UnsupportedOperationException.class);
    }
}
