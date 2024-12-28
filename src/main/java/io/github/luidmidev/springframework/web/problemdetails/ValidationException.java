package io.github.luidmidev.springframework.web.problemdetails;

import io.github.luidmidev.springframework.web.problemdetails.schemas.ValidationErrors;
import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {

    private final transient ValidationErrors validationErrors;

    public ValidationException(ValidationErrors validationErrors) {
        super("Validation error");
        this.validationErrors = validationErrors;
    }
}
