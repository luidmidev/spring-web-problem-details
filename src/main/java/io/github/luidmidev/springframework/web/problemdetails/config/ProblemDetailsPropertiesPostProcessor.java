package io.github.luidmidev.springframework.web.problemdetails.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.BeanPostProcessor;


/**
 * Bean post processor to inject the {@link ProblemDetailsProperties} configuration into beans that implement {@link ProblemDetailsPropertiesAware}.
 */
@Log4j2
@RequiredArgsConstructor
public class ProblemDetailsPropertiesPostProcessor implements BeanPostProcessor {

    private final ProblemDetailsProperties properties;

    /**
     * Process the bean before initialization.
     * @param bean the new bean instance
     * @param beanName the name of the bean
     * @return the bean instance to use, either the original or a wrapped one
     */
    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) {

        if (bean instanceof ProblemDetailsPropertiesAware aware) {
            log.debug("Setting errors configuration for bean {}", beanName);
            aware.setErrorsConfiguration(properties);
        }

        return bean;
    }

}
