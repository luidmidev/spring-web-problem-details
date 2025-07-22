package io.github.luidmidev.springframework.web.problemdetails;

import io.github.luidmidev.springframework.web.problemdetails.config.ProblemDetailsConfigurationPostProcessor;
import io.github.luidmidev.springframework.web.problemdetails.config.ProblemDetailsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(ProblemDetailsProperties.class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ProblemDetailsAutoConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public ResponseEntityExceptionHandlerResolver responseEntityExceptionHandlerResolver(ApplicationContext context) {
        log.debug("Creating ResponseEntityExceptionHandlerResolver");
        return new ResponseEntityExceptionHandlerResolver(context);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static ProblemDetailsConfigurationPostProcessor problemDetailsConfigurationPostProcessor(ProblemDetailsProperties properties, ResponseEntityExceptionHandlerResolver resolver) {
        log.debug("Creating ProblemDetailsConfigurationPostProcessor with properties {} and resolver {}", properties, resolver);
        return new ProblemDetailsConfigurationPostProcessor(properties, resolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultProblemDetailsExceptionHandler defaultExceptionHandler() {
        log.debug("Creating default exception handler");
        return new DefaultProblemDetailsExceptionHandler();
    }
}