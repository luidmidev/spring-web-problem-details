package io.github.luidmidev.springframework.errors.test;

import io.github.luidmidev.springframework.errors.DefaultExceptionHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Default exception handler for test profile
 */
@Log4j2
@Profile("test")
@ControllerAdvice
public class TestDefaultExceptionHandler extends DefaultExceptionHandler {

}
