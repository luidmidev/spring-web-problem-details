package io.github.luidmidev.springframework.web.problemdetails;

import io.github.luidmidev.springframework.web.problemdetails.config.ProblemDetailsPropertiesPostProcessor;
import io.github.luidmidev.springframework.web.problemdetails.config.ProblemDetailsProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(ProblemDetailsProperties.class)
public class ProblemDetailsAutoConfiguration {

    @Bean
    public ProblemDetailsPropertiesPostProcessor errorBeanPostProcessor(ProblemDetailsProperties properties) {
        return new ProblemDetailsPropertiesPostProcessor(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    DefaultProblemDetailsExceptionHandler defaultExceptionHandler() {
        return new DefaultProblemDetailsExceptionHandler();
    }
}
