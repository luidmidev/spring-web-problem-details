package io.github.luidmidev.springframework.errors.autoconfiguration;

import io.github.luidmidev.springframework.errors.config.ErrorBeanPostProcessor;
import io.github.luidmidev.springframework.errors.config.ErrorsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@org.springframework.boot.autoconfigure.AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(ErrorsProperties.class)
public class AutoConfiguration {

    @Bean
    public ErrorBeanPostProcessor errorBeanPostProcessor(ErrorsProperties properties) {
        return new ErrorBeanPostProcessor(properties);
    }
}
