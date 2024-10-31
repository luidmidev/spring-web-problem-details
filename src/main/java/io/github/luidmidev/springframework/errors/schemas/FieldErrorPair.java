package io.github.luidmidev.springframework.errors.schemas;

/**
 * Pair of field and message
 * @param field field
 * @param message message
 */
public record FieldErrorPair(String field, String message) {
}
