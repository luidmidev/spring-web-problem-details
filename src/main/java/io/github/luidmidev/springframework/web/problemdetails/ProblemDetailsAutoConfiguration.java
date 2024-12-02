package io.github.luidmidev.springframework.web.problemdetails;

import io.github.luidmidev.springframework.web.problemdetails.config.ProblemDetailsConfigurationPostProcessor;
import io.github.luidmidev.springframework.web.problemdetails.config.ProblemDetailsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(ProblemDetailsProperties.class)
public class ProblemDetailsAutoConfiguration {

    @Bean
    public ResponseEntityExceptionHandlerResolver responseEntityExceptionHandlerResolver(ApplicationContext context) {
        log.info("Creating ResponseEntityExceptionHandlerResolver");
        return new ResponseEntityExceptionHandlerResolver(context);
    }

    @Bean
    public  ProblemDetailsConfigurationPostProcessor errorBeanPostProcessor(ProblemDetailsProperties properties, ResponseEntityExceptionHandlerResolver resolver) {
        log.info("Creating ProblemDetailsConfiurationPostProcessor with properties {} and resolver {}", properties, resolver);
        return new ProblemDetailsConfigurationPostProcessor(properties, resolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public  DefaultProblemDetailsExceptionHandler defaultExceptionHandler() {
        log.info("Creating default exception handler");
        return new DefaultProblemDetailsExceptionHandler();
    }
}
