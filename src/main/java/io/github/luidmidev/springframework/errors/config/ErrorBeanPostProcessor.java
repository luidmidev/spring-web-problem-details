package io.github.luidmidev.springframework.errors.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;


/**
 * Bean post processor to inject the {@link ErrorsProperties} configuration into beans that implement {@link ErrorsPropertiesAware}.
 */
@Log4j2
@RequiredArgsConstructor
@Component
public class ErrorBeanPostProcessor implements BeanPostProcessor {

    private final ErrorsProperties properties;

    /**
     * Process the bean before initialization.
     * @param bean the new bean instance
     * @param beanName the name of the bean
     * @return the bean instance to use, either the original or a wrapped one
     */
    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) {

        if (bean instanceof ErrorsPropertiesAware aware) {
            log.info("Setting errors configuration for bean {}", beanName);
            aware.setErrorsConfiguration(properties);
        }

        return bean;
    }

}
