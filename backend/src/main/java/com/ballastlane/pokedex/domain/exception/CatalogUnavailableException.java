package com.ballastlane.pokedex.domain.exception;

/** The external Pokemon catalog (PokeAPI) could not be reached or returned an unexpected response. */
public class CatalogUnavailableException extends RuntimeException {

    public CatalogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
