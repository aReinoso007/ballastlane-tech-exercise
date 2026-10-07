package com.ballastlane.pokedex.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PokemonNameMatcherTest {

    private static final List<PokemonName> NAMES = List.of(
            name(1, "bulbasaur"), name(2, "ivysaur"), name(3, "venusaur"),
            name(4, "charmander"), name(5, "charmeleon"), name(6, "charizard"),
            name(7, "squirtle"), name(25, "pikachu"), name(26, "raichu"), name(172, "pichu"),
            name(122, "mr-mime"), name(133, "eevee"), name(10080, "pikachu-rock-star"), name(10094, "pikachu-unova-cap"));

    private static PokemonName name(int id, String name) {
        return new PokemonName(id, name, "https://img/" + id + ".png");
    }

    private static List<String> names(String query, int limit) {
        return PokemonNameMatcher.rank(query, NAMES, limit).stream().map(PokemonName::name).toList();
    }

    @Test
    void exactMatchComesFirst() {
        assertThat(names("pikachu", 5)).first().isEqualTo("pikachu");
    }

    @Test
    void isCaseAndWhitespaceInsensitive() {
        assertThat(names("  PiKaChU ", 1)).containsExactly("pikachu");
    }

    @Test
    void prefixMatchesRankShorterNamesFirst() {
        assertThat(names("pi", 6)).containsExactly("pichu", "pikachu", "pikachu-rock-star", "pikachu-unova-cap");
    }

    @Test
    void substringMatchesAreFound() {
        assertThat(names("saur", 5)).containsExactlyInAnyOrder("bulbasaur", "ivysaur", "venusaur");
    }

    @Test
    void toleratesATypo() {
        assertThat(names("pikachuu", 3)).first().isEqualTo("pikachu");
        assertThat(names("charzard", 3)).first().isEqualTo("charizard");
        assertThat(names("squirtel", 3)).first().isEqualTo("squirtle");
    }

    @Test
    void aTypoOfTheWholeNameBeatsLongerNamesThatStartWithIt() {
        assertThat(names("pikachuu", 3)).first().isEqualTo("pikachu");
    }

    @Test
    void toleratesTransposedLetters() {
        assertThat(names("bulbasuar", 3)).first().isEqualTo("bulbasaur");
        assertThat(names("pikahcu", 3)).first().isEqualTo("pikachu");
    }

    @Test
    void toleratesATypoWhileStillTypingTheName() {
        assertThat(names("charzar", 3)).first().isEqualTo("charizard");
    }

    @Test
    void ignoresPunctuationAndSpacesInTheQuery() {
        assertThat(names("mr mime", 1)).containsExactly("mr-mime");
        assertThat(names("mrmime", 1)).containsExactly("mr-mime");
    }

    @Test
    void findsByPokedexNumber() {
        assertThat(names("133", 5)).containsExactly("eevee");
    }

    @Test
    void doesNotInventMatchesForGibberishOrVeryShortQueries() {
        assertThat(names("xqzwv", 5)).isEmpty();
        assertThat(names("zz", 5)).isEmpty();
    }

    @Test
    void respectsTheLimit() {
        assertThat(names("c", 2)).hasSize(2);
    }

    @Test
    void blankQueryYieldsNothing() {
        assertThat(names("   ", 5)).isEmpty();
        assertThat(names("!!!", 5)).isEmpty();
    }
}
