package io.github.luidmidev.springframework.web.problemdetails.config;

import org.springframework.beans.factory.Aware;

/**
 * Interface to be implemented by any object that wishes to be notified of the {@link ProblemDetailsProperties} configuration.
 * @see ProblemDetailsProperties
 */
public interface ProblemDetailsPropertiesAware extends Aware {
    /**
     * Processor method
     * @param properties the configuration object.
     */
    void setErrorsConfiguration(ProblemDetailsProperties properties);
}
