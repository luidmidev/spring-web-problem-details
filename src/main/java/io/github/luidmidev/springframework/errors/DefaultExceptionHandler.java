package io.github.luidmidev.springframework.errors;

import io.github.luidmidev.springframework.errors.config.ErrorsProperties;
import io.github.luidmidev.springframework.errors.config.ErrorsPropertiesAware;
import io.github.luidmidev.springframework.errors.schemas.FieldErrorPair;
import io.github.luidmidev.springframework.errors.schemas.ValidationError;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;
import java.util.function.Function;

import static org.springframework.http.HttpStatus.*;

/**
 * Default Exception Handler
 */
@Log4j2
public abstract class DefaultExceptionHandler extends ResponseEntityExceptionHandler implements ErrorsPropertiesAware {

    private boolean allErrors;
    private boolean logErrors;
    private boolean sendStackTrace;

    /**
     * Processor method for the {@link ErrorsProperties} configuration.
     * @param properties the configuration object.
     */
    @Override
    public void setErrorsConfiguration(ErrorsProperties properties) {
        log.info("Setting error properties: {}", properties);
        this.allErrors = properties.isAllErrors();
        this.logErrors = properties.isLogErrors();
        this.sendStackTrace = properties.isSendStackTrace();
    }

    /**
     * Set the MessageSource that this object will use for resolving messages.
     * @param messageSource message source to be used by this object
     */
    @Override
    public void setMessageSource(@NotNull MessageSource messageSource) {
        log.info("Setting message source: {}", messageSource);
        log.info("Message source class: {}", messageSource.getClass());
        super.setMessageSource(messageSource);
    }

    /**
     * Handler for generic exceptions, it returns a ProblemDetail with the exception message if
     * {@link ErrorsProperties} allErrors is true, otherwise it returns a generic message.
     * @param ex the exception
     * @param request the web request
     * @return response entity with the problem detail.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleDefaultException(Exception ex, WebRequest request) {
        return allErrors
                ? createDefaultResponseEntity(ex, new HttpHeaders(), INTERNAL_SERVER_ERROR, ex.getMessage(), "problemDetail.java.lang.Exception.message", new Object[]{ex.getMessage()}, request)
                : createDefaultResponseEntity(ex, new HttpHeaders(), INTERNAL_SERVER_ERROR, "Internal Server Error", "problemDetail.java.lang.Exception", null, request);
    }

    /**
     * Handler for {@link ApiErrorException} exceptions.
     * @param ex the exception
     * @return response entity with the problem detail.
     */
    @ExceptionHandler(ApiErrorException.class)
    public ResponseEntity<ProblemDetail> handleApiErrorException(ApiErrorException ex) {
        var problem = ex.getBody();
        return ResponseEntity.status(problem.getStatus()).headers(ex.getHeaders()).body(problem);
    }

    /**
     * Handler for {@link ConstraintViolationException} exceptions.
     * @param ex the exception
     * @param request the web request
     * @return response entity with the problem detail.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        var defaultDetail = "One or more fields are invalid.";
        var body = super.createProblemDetail(ex, BAD_REQUEST, defaultDetail, null, null, request);

        addValidationErrors(body, ex.getConstraintViolations(), violation -> {
            var path = violation.getPropertyPath().toString().split("\\.");
            log.debug("Property path: {}, Class bean {}", violation.getPropertyPath(), violation.getRootBeanClass());
            return new FieldErrorPair(path[path.length - 1], violation.getMessage());
        });

        return createResponseEntity(body, new HttpHeaders(), BAD_REQUEST, request);
    }

    /**
     * Handler for authentication exceptions. The `createDefaultResponseEntity` method is not used because Spring Security is responsible for
     * internationalizing the authentication error message. This message is used only for the 'detail' of the ProblemDetail,
     * while the title and type are obtained from the MessageSource of this artifact.
     *
     * @param ex AuthenticationException
     * @param request WebRequest
     * @return response entity with the problem detail.
     *
     * @see SpringSecurityMessageSource#getAccessor()
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        var defaultDetail = ex.getMessage();
        var body = ProblemDetail.forStatusAndDetail(UNAUTHORIZED, defaultDetail);
        updateDefaultTittleAndType(ex, body);
        dispatchEvents(ex, body);
        return createResponseEntity(body, new HttpHeaders(), UNAUTHORIZED, request);
    }

    /**
     * Internal override handler method that all others in this class delegate to, for
     * common handling, and for the creation of a {@link ResponseEntity} with the
     * {@link ProblemDetail} body, after dispatching events.
     *
     * <p>The default implementation does the following:
     * <ul>
     * <li>return {@code null} if response is already committed
     * <li>set the {@code "jakarta.servlet.error.exception"} request attribute
     * if the response status is 500 (INTERNAL_SERVER_ERROR).
     * <li>extract the {@link ErrorResponse#getBody() body} from
     * {@link ErrorResponse} exceptions, if the {@code body} is {@code null}.
     * </ul>
     * @param ex the exception to handle
     * @param body the body to use for the response
     * @param headers the headers to use for the response
     * @param statusCode the status code to use for the response
     * @param request the current request
     * @return a {@code ResponseEntity} for the response to use, possibly
     * {@code null} when the response is already committed
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(@NotNull Exception ex, @Nullable Object body, @NotNull HttpHeaders headers, @NotNull HttpStatusCode statusCode, @NotNull WebRequest request) {
        var response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        dispatchEvents(ex, response != null ? response.getBody() : body);
        return response;
    }


    /**
     * Creates a new {@link ProblemDetail} object with the given parameters.
     * @param ex the exception
     * @param headers the headers
     * @param statusCode the status code
     * @param defaultDetail the default detail message
     * @param detailMessageCode the detail message code
     * @param detailMessageArguments the detail message arguments
     * @param request the web request
     * @return a new {@link ProblemDetail} object
     */
    protected ResponseEntity<Object> createDefaultResponseEntity(Exception ex, HttpHeaders headers, HttpStatusCode statusCode, String defaultDetail, @Nullable String detailMessageCode, Object[] detailMessageArguments, WebRequest request) {
        var body = super.createProblemDetail(ex, statusCode, defaultDetail, detailMessageCode, detailMessageArguments, request);
        dispatchEvents(ex, body);
        return createResponseEntity(body, headers, statusCode, request);
    }

    /**
     * Gnerate and add validation errors to the @{@link ProblemDetail} object.
     * @param body the problem detail object
     * @param errors the collection of errors
     * @param mapper the function to map the error to a {@link FieldErrorPair}
     * @param <T> the type of the error
     */
    protected static <T> void addValidationErrors(ProblemDetail body, Collection<T> errors, Function<T, FieldErrorPair> mapper) {
        var validations = new ValidationError();
        for (var error : errors) {
            var fieldError = mapper.apply(error);
            if (fieldError.field() != null) validations.addError(fieldError.field(), fieldError.message());
            else validations.addGlobalError(fieldError.message());
        }

        var errorsMap = validations.getErrors();
        var globalErrors = validations.getGlobalErrors();

        if (!errorsMap.isEmpty()) body.setProperty("errors", errorsMap);
        if (!globalErrors.isEmpty()) body.setProperty("globalErrors", globalErrors);
    }

    /**
     * Dispatch events after handling an exception.
     * @param ex the exception
     * @param body the body
     */
    private void dispatchEvents(Exception ex, Object body) {
        if (logErrors) log.error("Error: {}", ex.getMessage(), ex);
        if (sendStackTrace && body instanceof ProblemDetail problemDetail) problemDetail.setProperty("stackTrace", getStackTrace(ex));
    }


    /**
     * Update the default title and type of the {@link ProblemDetail} object.
     * @param ex the exception
     * @param body the problem detail object
     */
    protected void updateDefaultTittleAndType(Exception ex, ProblemDetail body) {
        var messageSource = getMessageSource();
        var clazz = ex.getClass();
        if (messageSource != null) {
            var locale = LocaleContextHolder.getLocale();
            body.setTitle(messageSource.getMessage(ErrorResponse.getDefaultTitleMessageCode(clazz), null, null, locale));
            var type = messageSource.getMessage(ErrorResponse.getDefaultTypeMessageCode(clazz), null, null, locale);
            if (type != null) body.setType(URI.create(type));
        }
    }

    /**
     * Get the stack trace of an exception as a string.
     * @param throwable the exception
     * @return the stack trace as a string
     */
    static String getStackTrace(Throwable throwable) {
        if (throwable == null) return "";
        var sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }
}
