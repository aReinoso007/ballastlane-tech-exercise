package com.ballastlane.pokedex.application.pokemon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Keeps the tag vocabulary consistent: the same tag typed with another case reuses the stored spelling. */
final class TagNames {

    private TagNames() {
    }

    /**
     * @param requested tags as typed by the user
     * @param known     tags already in the library
     * @return the requested tags using the library spelling where one exists, without duplicates ignoring case
     */
    static List<String> reuseKnownSpelling(List<String> requested, List<String> known) {
        List<String> result = new ArrayList<>();
        for (String tag : requested) {
            if (tag == null || tag.isBlank()) {
                continue;
            }
            String trimmed = tag.trim();
            String spelled = known.stream().filter(k -> k.equalsIgnoreCase(trimmed)).findFirst().orElse(trimmed);
            boolean duplicate = result.stream().anyMatch(r -> r.toLowerCase(Locale.ROOT).equals(spelled.toLowerCase(Locale.ROOT)));
            if (!duplicate) {
                result.add(spelled);
            }
        }
        return result;
    }
}
