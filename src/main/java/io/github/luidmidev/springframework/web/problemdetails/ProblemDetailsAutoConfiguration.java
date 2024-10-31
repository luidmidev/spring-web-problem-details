package io.github.luidmidev.springframework.web.problemdetails;

import io.github.luidmidev.springframework.web.problemdetails.config.ProblemDetailsPropertiesPostProcessor;
import io.github.luidmidev.springframework.web.problemdetails.config.ProblemDetailsProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Log4j2
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
        log.info("Creating default exception handler");
        return new DefaultProblemDetailsExceptionHandler();
    }
}
