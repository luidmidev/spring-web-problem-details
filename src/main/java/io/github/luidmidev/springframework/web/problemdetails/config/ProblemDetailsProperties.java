package io.github.luidmidev.springframework.web.problemdetails.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the errors module.
 * @see ProblemDetailsPropertiesAware
 */
@Data
@ConfigurationProperties(prefix = "spring.web.problemdetails")
public class ProblemDetailsProperties {
    /**
     * If true, all exceptions will be treated as errors, include supper class of {@link Exception}
     */
    private boolean allErrors = false;
    /**
     * If true, all stack traces will logged to the console
     */
    private boolean logErrors = false;

    /**
     * If true, all stack traces will be sent to the client
     */
    private boolean sendStackTrace = false;
}
