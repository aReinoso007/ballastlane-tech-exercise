package com.ballastlane.pokedex.domain.model;

/**
 * One node of an evolutionary lineage, flattened in traversal order.
 *
 * @param stage        0 for the base form, 1 for its evolutions, and so on
 * @param evolvesFrom  name of the previous form, or {@code null} for the base form
 */
public record EvolutionStage(int id, String name, String spriteUrl, int stage, String evolvesFrom) {
}
