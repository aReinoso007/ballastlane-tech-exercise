package com.ballastlane.pokedex.domain.exception;

import java.util.List;

/** Thrown when input or an entity violates a business/validation rule. Maps to HTTP 400. */
public class InvalidDataException extends RuntimeException {

    private final List<String> violations;

    public InvalidDataException(List<String> violations) {
        super(String.join("; ", violations));
        this.violations = List.copyOf(violations);
    }

    public InvalidDataException(String violation) {
        this(List.of(violation));
    }

    public List<String> violations() {
        return violations;
    }
}
