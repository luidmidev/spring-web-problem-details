package io.github.luidmidev.springframework.web.problemdetails.schemas;

/**
 * Pair of field and message
 * @param field field
 * @param message message
 */
public record FieldMessage(String field, String message) {
}
