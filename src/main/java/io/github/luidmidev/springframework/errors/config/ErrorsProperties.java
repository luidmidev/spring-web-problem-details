package io.github.luidmidev.springframework.errors.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the errors module.
 * @see ErrorsPropertiesAware
 */
@Data
@ConfigurationProperties(prefix = "io.github.luidmidev.springframework.errors")
public class ErrorsProperties {
    private boolean allErrors = false;
    private boolean logErrors = false;
    private boolean sendStackTrace = false;
}
