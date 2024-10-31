package io.github.luidmidev.springframework.errors.config;

import org.springframework.beans.factory.Aware;

/**
 * Interface to be implemented by any object that wishes to be notified of the {@link ErrorsProperties} configuration.
 * @see ErrorsProperties
 */
public interface ErrorsPropertiesAware extends Aware {
    /**
     * Processor method
     * @param properties the configuration object.
     */
    void setErrorsConfiguration(ErrorsProperties properties);
}
