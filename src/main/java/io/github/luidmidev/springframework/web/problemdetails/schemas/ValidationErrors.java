package io.github.luidmidev.springframework.web.problemdetails.schemas;

import io.github.luidmidev.springframework.web.problemdetails.ValidationException;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model used to represent validation errors.
 */
@Data
public class ValidationErrors {

    private final List<Error> errors = new ArrayList<>();
    private final List<String> globalErrors = new ArrayList<>();

    /**
     * Adds a new error for the given field if it already exists, otherwise creates a new one.
     * @param field The field name
     * @param message The error message
     */
    public void add(String field, String message) {
        var error = errors.stream()
                .filter(err -> err.getField().equals(field))
                .findFirst()
                .orElseGet(() -> new Error(field, message));
        error.getMessages().add(message);
    }

    /**
     * Adds a new error for the given field and throws a {@link ValidationException}.
     * @param field The field name
     * @param message The error message
     */
    public void addThrow(String field, String message) {
        add(field, message);
        throwThis();
    }

    /**
     * Adds a new global error.
     * @param message The error message
     */
    public void addGlobal(String message) {
        globalErrors.add(message);
    }

    /**
     * Adds a new error for the given field.
     * @return The error
     */
    public boolean hasErrors() {
        return !errors.isEmpty() || !globalErrors.isEmpty();
    }

    public void throwIfHasErrors() {
        if (hasErrors()) {
            throwThis();
        }
    }

    private void throwThis() {
        throw new ValidationException(this);
    }

    /**
     * Model used to represent a validation error for a specific field.
     */
    @Data
    @RequiredArgsConstructor
    public static class Error {
        private final String field;
        private List<String> messages;

        private Error(String field, List<String> messages) {
            this.field = field;
            this.messages = messages;
        }

        private Error(String field, String message) {
            this(field, new ArrayList<>(Collections.singletonList(message)));
        }
    }
}
