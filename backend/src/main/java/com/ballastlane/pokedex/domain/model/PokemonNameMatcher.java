package com.ballastlane.pokedex.domain.model;

import java.text.Normalizer;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Ranks catalog names against what a user typed, tolerating typos.
 *
 * <p>Order of relevance: exact match, typo of the whole name, prefix, substring, typo in a partly typed name (Damerau-Levenshtein edit
 * distance, so a swapped pair of letters counts as a single mistake). Punctuation, case, accents
 * and spaces are ignored, so "mr mime" finds "mr-mime". A numeric query matches the Pokedex number.
 */
public final class PokemonNameMatcher {

    private static final int EXACT = 0;
    private static final int TYPO_OF_NAME = 1;
    private static final int PREFIX = 2;
    private static final int SUBSTRING = 3;
    private static final int TYPO_WHILE_TYPING = 4;

    private PokemonNameMatcher() {
    }

    public static List<PokemonName> rank(String query, Collection<PokemonName> candidates, int limit) {
        String q = normalize(query);
        if (q.isEmpty() || limit <= 0) {
            return List.of();
        }
        boolean numeric = q.chars().allMatch(Character::isDigit);
        return candidates.stream()
                .map(candidate -> numeric ? matchNumber(q, candidate) : matchName(q, candidate))
                .filter(match -> match != null)
                .sorted(Comparator.comparingInt(Match::tier)
                        .thenComparingInt(Match::penalty)
                        .thenComparingInt(match -> match.name().id()))
                .limit(limit)
                .map(Match::name)
                .toList();
    }

    private static Match matchNumber(String q, PokemonName candidate) {
        return q.equals(String.valueOf(candidate.id())) ? new Match(candidate, EXACT, 0) : null;
    }

    private static Match matchName(String q, PokemonName candidate) {
        String n = normalize(candidate.name());
        if (n.equals(q)) {
            return new Match(candidate, EXACT, 0);
        }
        int allowed = allowedMistakes(q.length());
        // "pikachuu" is a typo of "pikachu": that beats longer names that merely start with those letters.
        int whole = allowed == 0 ? Integer.MAX_VALUE : distance(q, n);
        if (whole <= allowed) {
            return new Match(candidate, TYPO_OF_NAME, whole);
        }
        if (n.startsWith(q)) {
            return new Match(candidate, PREFIX, n.length() - q.length());
        }
        int index = n.indexOf(q);
        if (index >= 0) {
            return new Match(candidate, SUBSTRING, index * 100 + n.length());
        }
        if (allowed == 0) {
            return null;
        }
        // A typo in the start of a name that is still being typed ("charzar"); costs one extra.
        int partial = distance(q, n.substring(0, Math.min(n.length(), q.length()))) + 1;
        return partial <= allowed
                ? new Match(candidate, TYPO_WHILE_TYPING, partial * 1000 + Math.abs(n.length() - q.length()))
                : null;
    }

    private static int allowedMistakes(int length) {
        if (length <= 3) {
            return 0;
        }
        return length <= 5 ? 1 : 2;
    }

    static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return decomposed.replaceAll("[^a-z0-9]", "");
    }

    /** Optimal string alignment distance: insert, delete, substitute or swap two neighbouring letters. */
    static int distance(String a, String b) {
        int[][] d = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            d[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            d[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + cost);
                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) {
                    d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + 1);
                }
            }
        }
        return d[a.length()][b.length()];
    }

    private record Match(PokemonName name, int tier, int penalty) {
    }
}
