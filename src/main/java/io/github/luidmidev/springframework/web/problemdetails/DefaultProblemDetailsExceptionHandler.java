package io.github.luidmidev.springframework.web.problemdetails;

import io.github.luidmidev.springframework.web.problemdetails.config.ProblemDetailsProperties;
import io.github.luidmidev.springframework.web.problemdetails.config.ProblemDetailsPropertiesAware;
import io.github.luidmidev.springframework.web.problemdetails.schemas.FieldMessage;
import io.github.luidmidev.springframework.web.problemdetails.schemas.ValidationErrorCollector;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
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
@ControllerAdvice
public class DefaultProblemDetailsExceptionHandler extends ResponseEntityExceptionHandler implements ProblemDetailsPropertiesAware {

    private boolean allErrors;
    private boolean logErrors;
    private boolean sendStackTrace;

    /**
     * Processor method for the {@link ProblemDetailsProperties} configuration.
     *
     * @param properties the configuration object.
     */
    @Override
    public void setErrorsConfiguration(ProblemDetailsProperties properties) {
        log.info("Setting error properties: {}", properties);
        this.allErrors = properties.isAllErrors();
        this.logErrors = properties.isLogErrors();
        this.sendStackTrace = properties.isSendStackTrace();
    }

    /**
     * Check if the {@link ProblemDetailsProperties} allErrors value
     *
     * @return value
     */
    protected boolean isAllErrors() {
        return allErrors;
    }

    /**
     * Check if the {@link ProblemDetailsProperties} logErrors value
     *
     * @return value
     */
    protected boolean isLogErrors() {
        return logErrors;
    }

    /**
     * Check if the {@link ProblemDetailsProperties} sendStackTrace value
     *
     * @return value
     */
    protected boolean isSendStackTrace() {
        return sendStackTrace;
    }

    /**
     * Set the MessageSource that this object will use for resolving messages.
     *
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
     * {@link ProblemDetailsProperties} allErrors is true, otherwise it returns a generic message.
     *
     * @param ex      the exception
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
     * Handler for {@link ProblemDetailsException} exceptions.
     *
     * @param ex the exception
     * @return response entity with the problem detail.
     */
    @ExceptionHandler(ProblemDetailsException.class)
    public ResponseEntity<ProblemDetail> handleApiErrorException(ProblemDetailsException ex) {
        var problem = ex.getBody();
        return ResponseEntity.status(problem.getStatus()).headers(ex.getHeaders()).body(problem);
    }


    /**
     * Handler for {@link ConstraintViolationException} exceptions.
     *
     * @param ex      the exception
     * @param request the web request
     * @return response entity with the problem detail.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        var defaultDetail = "One or more fields are invalid.";
        var body = super.createProblemDetail(ex, BAD_REQUEST, defaultDetail, null, null, request);

        addValidationErrors(body, ex.getConstraintViolations(), violation -> {
            log.debug("Property path: {}, Class bean {}", violation.getPropertyPath(), violation.getRootBeanClass());
            var path = violation.getPropertyPath().toString();
            return new FieldMessage(path, violation.getMessage());
        });

        return createResponseEntity(ex, new HttpHeaders(), BAD_REQUEST, request, body);
    }

    /**
     * Handler for authentication exceptions. Spring Security is responsible for internationalizing the authentication error message.
     * This message is used only for the 'detail' of the ProblemDetail, while the title and type are obtained from the MessageSource
     * of this artifact.
     *
     * @param ex      AuthenticationException
     * @param request WebRequest
     * @return response entity with the problem detail.
     * @see SpringSecurityMessageSource#getAccessor()
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        return createDefaultResponseEntity(ex, new HttpHeaders(), UNAUTHORIZED, ex.getMessage(), null, null, request);
    }

    /**
     * Handler for authorization exceptions. Spring Security is responsible for internationalizing the authorization error message.
     * This message is used only for the 'detail' of the ProblemDetail, while the title and type are obtained from the MessageSource
     * of this artifact.
     *
     * @param ex      AccessDeniedException
     * @param request WebRequest
     * @return response entity with the problem detail.
     * @see SpringSecurityMessageSource#getAccessor()
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAuthorizationDeniedException(AccessDeniedException ex, WebRequest request) {
        return createDefaultResponseEntity(ex, new HttpHeaders(), FORBIDDEN, ex.getMessage(), null, null, request);
    }



    /**
     * Creates a new {@link ProblemDetail} object with the given parameters.
     *
     * @param ex                     the exception
     * @param headers                the headers
     * @param statusCode             the status code
     * @param defaultDetail          the default detail message, if the message code is not found
     * @param detailMessageCode      the detail message code to use, if not exists the default detail message is used
     * @param detailMessageArguments the detail message arguments use to interpolate the message
     * @param request                the web request
     * @return a new {@link ProblemDetail} object
     */
    protected ResponseEntity<Object> createDefaultResponseEntity(Exception ex, HttpHeaders headers, HttpStatusCode statusCode, String defaultDetail, @Nullable String detailMessageCode, Object[] detailMessageArguments, WebRequest request) {
        var body = super.createProblemDetail(ex, statusCode, defaultDetail, detailMessageCode, detailMessageArguments, request);
        return createResponseEntity(ex, headers, statusCode, request, body);
    }


    /**
     * Gnerate and add validation errors to the @{@link ProblemDetail} object.
     *
     * @param body   the problem detail object
     * @param errors the collection of errors
     * @param mapper the function to map the error to a {@link FieldMessage}
     * @param <T>    the type of the error
     */
    protected static <T> void addValidationErrors(ProblemDetail body, Collection<T> errors, Function<T, FieldMessage> mapper) {
        var validations = new ValidationErrorCollector();
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
     *
     * @param ex         the exception to handle
     * @param body       the body to use for the response
     * @param headers    the headers to use for the response
     * @param statusCode the status code to use for the response
     * @param request    the current request
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
     * Create a new {@link ResponseEntity} object with the given parameters and dispatch events.
     *
     * @param ex         the exception to handle
     * @param headers    the headers
     * @param statusCode the status code
     * @param request    the current request
     * @param body       the body to use for the response
     * @return a {@code ResponseEntity} for the response to use
     */
    private ResponseEntity<Object> createResponseEntity(@NotNull Exception ex, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request, ProblemDetail body) {
        dispatchEvents(ex, body);
        return createResponseEntity(body, headers, statusCode, request);
    }

    /**
     * Update the default title and type of the {@link ProblemDetail} object.
     *
     * @param ex   the exception
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
     * Dispatch events after handling an exception.
     *
     * @param ex   the exception
     * @param body the body
     */
    private void dispatchEvents(Exception ex, Object body) {
        if (logErrors) log.error("Error: {}", ex.getMessage(), ex);
        if (sendStackTrace && body instanceof ProblemDetail problemDetail) problemDetail.setProperty("stackTrace", getStackTrace(ex));
    }


    /**
     * Get the stack trace of an exception as a string.
     *
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
