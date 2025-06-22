package io.github.luidmidev.springframework.web.problemdetails.config;

import io.github.luidmidev.springframework.web.problemdetails.ResponseEntityExceptionHandlerResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.BeanPostProcessor;


/**
 * Bean post processor to inject the {@link ProblemDetailsProperties} configuration into beans that implement {@link ProblemDetailsPropertiesAware}.
 */
@Slf4j
@RequiredArgsConstructor
public class ProblemDetailsConfigurationPostProcessor implements BeanPostProcessor {

    private final ProblemDetailsProperties properties;
    private final ResponseEntityExceptionHandlerResolver resolver;

    /**
     * Process the bean before initialization.
     * @param bean the new bean instance
     * @param beanName the name of the bean
     * @return the bean instance to use, either the original or a wrapped one
     */
    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) {
        log.trace("Processing bean {} of type {}", beanName, bean.getClass().getName());

        if (bean instanceof ProblemDetailsPropertiesAware aware) {
            log.trace("Injecting ProblemDetailsProperties into bean {}", beanName);
            aware.setProblemDetailsProperties(properties);
        }

        if (bean instanceof ResponseEntityExceptionHandlerResolverAware aware) {
            log.trace("Injecting ResponseEntityExceptionHandlerResolver into bean {}", beanName);
            aware.setResponseEntityExceptionHandlerResolver(resolver);
        }

        return bean;
    }

}
