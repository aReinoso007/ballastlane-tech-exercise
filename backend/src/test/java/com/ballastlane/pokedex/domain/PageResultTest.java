package com.ballastlane.pokedex.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.ballastlane.pokedex.domain.model.PageResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class PageResultTest {

    @Test
    void computesTotalPagesRoundingUp() {
        assertThat(new PageResult<>(List.of(1), 0, 20, 41).totalPages()).isEqualTo(3);
        assertThat(new PageResult<>(List.of(1), 0, 20, 40).totalPages()).isEqualTo(2);
        assertThat(new PageResult<>(List.<Integer>of(), 0, 20, 0).totalPages()).isZero();
    }
}
