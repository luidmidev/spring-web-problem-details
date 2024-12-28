package io.github.luidmidev.springframework.web.problemdetails.schemas;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Model used to represent validation errors.
 */
@Data
public class ValidationErrorCollector {

    private final List<Error> errors = new ArrayList<>();
    private final List<String> globalErrors = new ArrayList<>();


    /**
     * Adds a new error for the given field if it already exists, otherwise creates a new one.
     * @param field The field name
     * @param message The error message
     */
    public void addError(String field, String message) {
        var error = errors.stream()
                .filter(err -> err.getField().equals(field))
                .findFirst()
                .orElseGet(() -> {
                    var err = new Error(field);
                    errors.add(err);
                    return err;
                });
        error.getMessages().add(message);
    }

    /**
     * Adds a new global error.
     * @param message The error message
     */
    public void addGlobalError(String message) {
        globalErrors.add(message);
    }

    public void throwIfHasErrors() {
        if (!errors.isEmpty() || !globalErrors.isEmpty()) {
            throw new ValidationException(this);
        }
    }

    /**
     * Model used to represent a validation error for a specific field.
     */
    @Data
    public static class Error {
        private final String field;
        private List<String> messages = new ArrayList<>();
    }
}
