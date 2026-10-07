package com.ballastlane.pokedex.web;

import com.ballastlane.pokedex.domain.exception.CatalogUnavailableException;
import com.ballastlane.pokedex.domain.exception.InvalidCredentialsException;
import com.ballastlane.pokedex.domain.exception.InvalidDataException;
import com.ballastlane.pokedex.domain.exception.PokemonAlreadySyncedException;
import com.ballastlane.pokedex.domain.exception.PokemonNotFoundException;
import com.ballastlane.pokedex.domain.exception.UserAlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Maps every failure to a consistent RFC 7807 {@code application/problem+json} body. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidDataException.class)
    ProblemDetail invalidData(InvalidDataException e) {
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", e.getMessage(), e.violations());
    }

    @ExceptionHandler(PokemonNotFoundException.class)
    ProblemDetail notFound(PokemonNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Not found", e.getMessage(), null);
    }

    @ExceptionHandler(PokemonAlreadySyncedException.class)
    ProblemDetail alreadySynced(PokemonAlreadySyncedException e) {
        return problem(HttpStatus.CONFLICT, "Conflict", e.getMessage(), null);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    ProblemDetail userExists(UserAlreadyExistsException e) {
        return problem(HttpStatus.CONFLICT, "Conflict", e.getMessage(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integrity(DataIntegrityViolationException e) {
        log.warn("Data integrity violation", e);
        return problem(HttpStatus.CONFLICT, "Conflict", "The request conflicts with existing data", null);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail invalidCredentials(InvalidCredentialsException e) {
        return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", e.getMessage(), null);
    }

    @ExceptionHandler(CatalogUnavailableException.class)
    ProblemDetail catalogUnavailable(CatalogUnavailableException e) {
        log.error("PokeAPI failure", e);
        return problem(HttpStatus.BAD_GATEWAY, "Upstream unavailable",
                "The Pokemon catalog (PokeAPI) is currently unavailable. Please try again later.", null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail constraintViolation(ConstraintViolationException e) {
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", "Request validation failed",
                e.getConstraintViolations().stream().map(v -> v.getPropertyPath() + ": " + v.getMessage()).toList());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail typeMismatch(MethodArgumentTypeMismatchException e) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid parameter",
                "Parameter '" + e.getName() + "' has an invalid value", null);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception e) {
        log.error("Unhandled exception", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", "An unexpected error occurred", null);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                 HttpHeaders headers, HttpStatusCode status,
                                                                 WebRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).toList();
        return ResponseEntity.badRequest()
                .body(problem(HttpStatus.BAD_REQUEST, "Validation failed", "Request validation failed", errors));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                 HttpHeaders headers, HttpStatusCode status,
                                                                 WebRequest request) {
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, "Malformed request",
                "The request body is missing or malformed", null));
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail, List<String> errors) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setTitle(title);
        if (errors != null && !errors.isEmpty()) {
            p.setProperty("errors", errors);
        }
        return p;
    }
}
