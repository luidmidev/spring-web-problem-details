package io.github.luidmidev.springframework.web.problemdetails.config;

import io.github.luidmidev.springframework.web.problemdetails.ResponseEntityExceptionHandlerResolver;
import org.springframework.beans.factory.Aware;

/**
 * Interface aware for {@link ResponseEntityExceptionHandlerResolver} bean
 */
public interface ResponseEntityExceptionHandlerResolverAware extends Aware {

    /**
     * Processor method
     * @param resolver the resolver object.
     */
    void setResponseEntityExceptionHandlerResolver(ResponseEntityExceptionHandlerResolver resolver);
}
