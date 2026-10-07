package com.ballastlane.pokedex.application.pokemon;

import com.ballastlane.pokedex.domain.exception.InvalidDataException;
import java.util.ArrayList;
import java.util.List;

final class Paging {

    private Paging() {
    }

    static void validate(int page, int size, int maxSize) {
        List<String> violations = new ArrayList<>();
        if (page < 0) {
            violations.add("page must be zero or greater");
        }
        if (size < 1 || size > maxSize) {
            violations.add("size must be between 1 and " + maxSize);
        }
        if (!violations.isEmpty()) {
            throw new InvalidDataException(violations);
        }
    }

    static String normalizeIdentifier(String idOrName) {
        if (idOrName == null || idOrName.isBlank()) {
            throw new InvalidDataException("Pokemon identifier must not be blank");
        }
        return idOrName.trim().toLowerCase();
    }
}
