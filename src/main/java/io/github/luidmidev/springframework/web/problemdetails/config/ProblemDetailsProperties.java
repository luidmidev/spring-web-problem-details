package io.github.luidmidev.springframework.web.problemdetails.config;

import lombok.Data;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Role;

/**
 * Configuration properties for the errors module.
 *
 * @see ProblemDetailsPropertiesAware
 */
@Data
@ConfigurationProperties(prefix = "spring.web.problemdetails")
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ProblemDetailsProperties {
    /**
     * If true, all exceptions will be treated as errors, include supper class of {@link Exception}
     */
    private boolean allErrors = false;

    /**
     * If true, all stack traces will be sent to the client
     */
    private boolean sendStackTrace = false;
}
