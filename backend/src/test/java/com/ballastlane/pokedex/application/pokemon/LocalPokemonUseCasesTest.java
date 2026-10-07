package com.ballastlane.pokedex.application.pokemon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ballastlane.pokedex.domain.exception.InvalidDataException;
import com.ballastlane.pokedex.domain.exception.PokemonAlreadySyncedException;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.model.Pokemon;
import com.ballastlane.pokedex.domain.model.PokemonEdit;
import com.ballastlane.pokedex.domain.port.PokemonCatalogPort;
import com.ballastlane.pokedex.support.Fixtures;
import com.ballastlane.pokedex.support.InMemoryPokemonRepository;
import com.ballastlane.pokedex.support.InMemoryTagRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LocalPokemonUseCasesTest {

    private InMemoryPokemonRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPokemonRepository();
    }

    @Nested
    class SyncPokemonTest {

        private PokemonCatalogPort catalog;
        private SyncPokemon useCase;

        @BeforeEach
        void init() {
            catalog = mock(PokemonCatalogPort.class);
            useCase = new SyncPokemon(catalog, repository);
        }

        @Test
        void storesRemotePokemonLocally() {
            when(catalog.fetch("bulbasaur")).thenReturn(Fixtures.bulbasaur());

            Pokemon saved = useCase.execute("Bulbasaur");

            assertThat(saved).isEqualTo(Fixtures.bulbasaur());
            assertThat(repository.existsById(1)).isTrue();
        }

        @Test
        void refusesToSyncTwice() {
            when(catalog.fetch("1")).thenReturn(Fixtures.bulbasaur());
            useCase.execute("1");

            assertThatThrownBy(() -> useCase.execute("1")).isInstanceOf(PokemonAlreadySyncedException.class);
            assertThat(repository.saveCalls).isEqualTo(1);
        }

        @Test
        void propagatesNotFoundWithoutSaving() {
            when(catalog.fetch("missing")).thenThrow(new PokemonNotFoundException("missing"));

            assertThatThrownBy(() -> useCase.execute("missing")).isInstanceOf(PokemonNotFoundException.class);
            assertThat(repository.saveCalls).isZero();
        }

        @Test
        void rejectsBlankIdentifier() {
            assertThatThrownBy(() -> useCase.execute(" ")).isInstanceOf(InvalidDataException.class);
        }
    }

    @Nested
    class ReadUseCasesTest {

        @BeforeEach
        void seed() {
            repository.save(Fixtures.bulbasaur());
            repository.save(Fixtures.pikachu());
            repository.saveCalls = 0;
        }

        @Test
        void listsPaginated() {
            var page = new ListLocalPokemon(repository).execute(0, 1);
            assertThat(page.items()).extracting(Pokemon::id).containsExactly(1);
            assertThat(page.totalItems()).isEqualTo(2);
            assertThat(page.totalPages()).isEqualTo(2);
        }

        @Test
        void rejectsInvalidPaging() {
            assertThatThrownBy(() -> new ListLocalPokemon(repository).execute(-1, 10))
                    .isInstanceOf(InvalidDataException.class);
            assertThatThrownBy(() -> new ListLocalPokemon(repository).execute(0, 101))
                    .isInstanceOf(InvalidDataException.class);
        }

        @Test
        void getsById() {
            assertThat(new GetLocalPokemon(repository).execute(25).name()).isEqualTo("pikachu");
        }

        @Test
        void getThrowsWhenMissing() {
            assertThatThrownBy(() -> new GetLocalPokemon(repository).execute(999))
                    .isInstanceOf(PokemonNotFoundException.class);
        }

        @Test
        void deletesExisting() {
            new DeleteLocalPokemon(repository).execute(1);
            assertThat(repository.existsById(1)).isFalse();
        }

        @Test
        void deleteThrowsWhenMissing() {
            assertThatThrownBy(() -> new DeleteLocalPokemon(repository).execute(999))
                    .isInstanceOf(PokemonNotFoundException.class);
        }
    }

    @Nested
    class UpdateLocalPokemonTest {

        private UpdateLocalPokemon useCase;
        private InMemoryTagRepository tagLibrary;

        @BeforeEach
        void seed() {
            repository.save(Fixtures.bulbasaur());
            tagLibrary = new InMemoryTagRepository().with("Favourite", "starter");
            useCase = new UpdateLocalPokemon(repository, tagLibrary);
        }

        @Test
        void patchUpdatesOnlyGivenFieldsAndPersists() {
            Pokemon updated = useCase.patch(1, PokemonEdit.builder().region("Kanto").tags(List.of("starter")).build());

            assertThat(updated.region()).isEqualTo("Kanto");
            assertThat(updated.name()).isEqualTo("bulbasaur");
            assertThat(repository.findById(1).orElseThrow().tags()).containsExactly("starter");
        }

        @Test
        void tagsAlreadyInTheLibraryKeepTheirStoredSpellingWhateverTheCase() {
            Pokemon updated = useCase.patch(1,
                    PokemonEdit.builder().tags(List.of("favourite", "STARTER", "brand new")).build());

            assertThat(updated.tags()).containsExactly("Favourite", "starter", "brand new");
        }

        @Test
        void sameTagTypedTwiceWithDifferentCaseIsStoredOnce() {
            Pokemon updated = useCase.patch(1, PokemonEdit.builder().tags(List.of("Rare", "rare", " RARE ")).build());

            assertThat(updated.tags()).containsExactly("Rare");
        }

        @Test
        void replaceAlsoReusesKnownTags() {
            Pokemon updated = useCase.replace(1, PokemonEdit.builder()
                    .name("bulbasaur").height(7).weight(69).abilities(List.of("overgrow"))
                    .tags(List.of("FAVOURITE")).build());

            assertThat(updated.tags()).containsExactly("Favourite");
        }

        @Test
        void patchWithoutTagsLeavesTagsAndLibraryUntouched() {
            repository.save(Fixtures.bulbasaur().applyPartial(PokemonEdit.builder().tags(List.of("grass")).build()));

            Pokemon updated = useCase.patch(1, PokemonEdit.builder().region("Kanto").build());

            assertThat(updated.tags()).containsExactly("grass");
        }

        @Test
        void patchOnMissingPokemonThrowsNotFound() {
            assertThatThrownBy(() -> useCase.patch(404, PokemonEdit.builder().region("x").build()))
                    .isInstanceOf(PokemonNotFoundException.class);
        }

        @Test
        void patchWithInvalidDataDoesNotPersist() {
            repository.saveCalls = 0;
            assertThatThrownBy(() -> useCase.patch(1, PokemonEdit.builder().weight(-5).build()))
                    .isInstanceOf(InvalidDataException.class);
            assertThat(repository.saveCalls).isZero();
            assertThat(repository.findById(1).orElseThrow().weight()).isEqualTo(69);
        }

        @Test
        void patchWithEmptyEditIsRejected() {
            assertThatThrownBy(() -> useCase.patch(1, PokemonEdit.builder().build()))
                    .isInstanceOf(InvalidDataException.class);
        }

        @Test
        void replaceOverwritesEditableFields() {
            Pokemon replaced = useCase.replace(1, PokemonEdit.builder()
                    .name("bulba").height(1).weight(2).abilities(List.of("overgrow")).build());

            assertThat(replaced.name()).isEqualTo("bulba");
            assertThat(repository.findById(1).orElseThrow().height()).isEqualTo(1);
        }

        @Test
        void replaceOnMissingPokemonThrowsNotFound() {
            assertThatThrownBy(() -> useCase.replace(404, PokemonEdit.builder().build()))
                    .isInstanceOf(PokemonNotFoundException.class);
        }
    }

    @Nested
    class ListTagsTest {

        @Test
        void returnsTheLibrary() {
            assertThat(new ListTags(new InMemoryTagRepository().with("a", "b")).execute()).containsExactly("a", "b");
        }
    }
}
