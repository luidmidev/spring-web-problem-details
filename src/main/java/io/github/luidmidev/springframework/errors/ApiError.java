package io.github.luidmidev.springframework.errors;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;


/**
 * Model class for API error response with formatt Problem Details for HTTP APIs (RFC 9457)
 */
@SuppressWarnings("unused")
@Getter
public class ApiError {

    // The HTTP status code
    private final HttpStatusCode status;

    // The headers to be added to the response
    private final HttpHeaders headers = new HttpHeaders();
    private String title;
    private URI type;
    private URI instance;
    private Map<String, Object> extensions = new LinkedHashMap<>();

    /**
     * Create a new instance of {@link ApiError} with {@link HttpStatus#BAD_REQUEST} status
     */
    public ApiError() {
        this(HttpStatus.BAD_REQUEST);
    }

    /**
     * Create a new instance of {@link ApiError} with the specified status
     * @param status the HTTP status code
     */
    public ApiError(HttpStatusCode status) {
        this.status = status;
    }

    /**
     * Create a new instance of {@link ApiError} with the specified status
     * @param httpStatusCode the HTTP status code
     * @return a new instance of {@link ApiError}
     */
    public static ApiError status(@NotNull HttpStatusCode httpStatusCode) {
        return new ApiError(httpStatusCode);
    }

    /**
     * Create a new instance of {@link ApiError} with the specified status
     * @param httpStatusCode the HTTP status code
     * @return a new instance of {@link ApiError}
     */
    public static ApiError status(int httpStatusCode) {
        return status(HttpStatusCode.valueOf(httpStatusCode));
    }

    /**
     * Assign a title to the problem detail and return the current instance
     * @param title the title of the error
     * @return the current instance
     */
    public ApiError title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Assign a type to the problem detail and return the current instance
     * @param type the type of the error
     * @return the current instance
     */
    public ApiError type(URI type) {
        this.type = type;
        return this;
    }

    /**
     * Assign a type to the problem detail and return the current instance
     * @param type the type of the error
     * @return the current instance
     */
    public ApiError type(String type) {
        return type(URI.create(type));
    }

    /**
     * Assign an instance to the problem detail and return the current instance
     * @param instance the instance of the error
     * @return the current instance
     */
    public ApiError instance(URI instance) {
        this.instance = instance;
        return this;
    }

    /**
     * Assign an instance to the problem detail and return the current instance
     * @param instance the instance of the error
     * @return the current instance
     */
    public ApiError instance(String instance) {
        return instance(URI.create(instance));
    }

    /**
     * Assign extensions attributes to the problem detail and return the current instance
     * @param extensions the extensions attribute of the error
     * @return the current instance
     */
    public ApiError extensions(Map<String, Object> extensions) {
        this.extensions = extensions;
        return this;
    }

    /**
     * Add an extension attribute to the problem detail and return the current instance
     * @param key the key of the extension attribute
     * @param value the value of the extension attribute
     * @return the current instance
     */
    public ApiError extension(String key, Object value) {
        this.extensions.put(key, value);
        return this;
    }

    /**
     * Add a header to the response and return the current instance
     * @param headerName the name of the header
     * @param headersValues the values of the header
     * @return the current instance
     */
    public ApiError header(String headerName, String... headersValues) {
        this.headers.addAll(headerName, List.of(headersValues));
        return this;
    }

    /**
     * Add a header to the response and return the current instance
     * @param headers the headers to add
     * @return the current instance
     */
    public ApiError headers(MultiValueMap<String, String> headers) {
        this.headers.addAll(headers);
        return this;
    }


    /**
     * Create a new instance of {@link ApiErrorException} with the current state of the {@link ApiError}
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public ApiErrorException detail(String detail) {
        var problemDetail = getProblemDetail(detail);
        return new ApiErrorException(problemDetail, headers);
    }

    /**
     * Create a new {@link ProblemDetail} with the current state of the {@link ApiError}
     * @param detail the detail of the error
     * @return a new instance of {@link ProblemDetail}
     */
    public ProblemDetail getProblemDetail(String detail) {
        var problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        if (type != null) problemDetail.setType(type);
        problemDetail.setTitle(title);
        problemDetail.setInstance(instance);
        if (!extensions.isEmpty()) problemDetail.setProperties(extensions);
        return problemDetail;
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with the specified title, detail and status
     * @param title the title of the error
     * @param detail the detail of the error
     * @param status the HTTP status code
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException fail(String title, String detail, HttpStatus status) {
        return status(status).title(title).detail(detail);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with the specified detail and status
     * @param detail the detail of the error
     * @param status the HTTP status code
     * @return a new instance of {@link ApiErrorException}
     */
    private static ApiErrorException fail(String detail, HttpStatus status) {
        return status(status).detail(detail);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with the specified title, detail and status
     * @param title the title of the error
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException notFound(String title, String detail) {
        return fail(title, detail, NOT_FOUND);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#BAD_REQUEST} status and the specified title and detail
     * @param title the title of the error
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException badRequest(String title, String detail) {
        return fail(title, detail, BAD_REQUEST);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#UNAUTHORIZED} status and the specified title and detail
     * @param title the title of the error
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException unauthorized(String title, String detail) {
        return fail(title, detail, UNAUTHORIZED);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#FORBIDDEN} status and the specified title and detail
     * @param title the title of the error
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException forbidden(String title, String detail) {
        return fail(title, detail, FORBIDDEN);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#CONFLICT} status and the specified title and detail
     * @param title the title of the error
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException conflict(String title, String detail) {
        return fail(title, detail, CONFLICT);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#PRECONDITION_FAILED} status and the specified title and detail
     * @param title the title of the error
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException preconditionFailed(String title, String detail) {
        return fail(title, detail, PRECONDITION_FAILED);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#PRECONDITION_REQUIRED} status and the specified title and detail
     * @param title the title of the error
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException preconditionRequired(String title, String detail) {
        return fail(title, detail, PRECONDITION_REQUIRED);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#TOO_MANY_REQUESTS} status and the specified title and detail
     * @param title the title of the error
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException tooManyRequests(String title, String detail) {
        return fail(title, detail, TOO_MANY_REQUESTS);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#INTERNAL_SERVER_ERROR} status and the specified title and detail
     * @param title the title of the error
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException internalServerError(String title, String detail) {
        return fail(title, detail, INTERNAL_SERVER_ERROR);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#NOT_IMPLEMENTED} status and the specified title and detail
     * @param title the title of the error
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException notImplemented(String title, String detail) {
        return fail(title, detail, NOT_IMPLEMENTED);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#SERVICE_UNAVAILABLE} status and the specified title and detail
     * @param title the title of the error
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException serviceUnavailable(String title, String detail) {
        return fail(title, detail, SERVICE_UNAVAILABLE);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#GATEWAY_TIMEOUT} status and the specified title and detail
     * @param title the title of the error
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException gatewayTimeout(String title, String detail) {
        return fail(title, detail, GATEWAY_TIMEOUT);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#BAD_GATEWAY} status and the specified title and detail
     * @param title the title of the error
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException badGateway(String title, String detail) {
        return fail(title, detail, BAD_GATEWAY);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#NOT_FOUND} status and the specified detail
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException notFound(String detail) {
        return fail(detail, NOT_FOUND);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#BAD_REQUEST} status and the specified detail
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException badRequest(String detail) {
        return fail(detail, BAD_REQUEST);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#UNAUTHORIZED} status and the specified detail
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException unauthorized(String detail) {
        return fail(detail, UNAUTHORIZED);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#FORBIDDEN} status and the specified detail
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException forbidden(String detail) {
        return fail(detail, FORBIDDEN);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#CONFLICT} status and the specified detail
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException conflict(String detail) {
        return fail(detail, CONFLICT);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#PRECONDITION_FAILED} status and the specified detail
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException preconditionFailed(String detail) {
        return fail(detail, PRECONDITION_FAILED);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#PRECONDITION_REQUIRED} status and the specified detail
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException preconditionRequired(String detail) {
        return fail(detail, PRECONDITION_REQUIRED);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#TOO_MANY_REQUESTS} status and the specified detail
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException tooManyRequests(String detail) {
        return fail(detail, TOO_MANY_REQUESTS);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#INTERNAL_SERVER_ERROR} status and the specified detail
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException internalServerError(String detail) {
        return fail(detail, INTERNAL_SERVER_ERROR);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#NOT_IMPLEMENTED} status and the specified detail
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException notImplemented(String detail) {
        return fail(detail, NOT_IMPLEMENTED);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#SERVICE_UNAVAILABLE} status and the specified detail
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException serviceUnavailable(String detail) {
        return fail(detail, SERVICE_UNAVAILABLE);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#GATEWAY_TIMEOUT} status and the specified detail
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException gatewayTimeout(String detail) {
        return fail(detail, GATEWAY_TIMEOUT);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#BAD_GATEWAY} status and the specified detail
     * @param detail the detail of the error
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException badGateway(String detail) {
        return fail(detail, BAD_GATEWAY);
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#NOT_FOUND} status and the default message
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException notFound() {
        return notFound("Not Found");
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#BAD_REQUEST} status and the default message
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException badRequest() {
        return badRequest("Bad Request");
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#UNAUTHORIZED} status and the default message
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException unauthorized() {
        return unauthorized("Unauthorized");
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#FORBIDDEN} status and the default message
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException forbidden() {
        return forbidden("Forbidden");
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#CONFLICT} status and the default message
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException conflict() {
        return conflict("Conflict");
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#PRECONDITION_FAILED} status and the default message
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException preconditionFailed() {
        return preconditionFailed("Precondition Failed");
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#PRECONDITION_REQUIRED} status and the default message
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException preconditionRequired() {
        return preconditionRequired("Precondition Required");
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#TOO_MANY_REQUESTS} status and the default message
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException tooManyRequests() {
        return tooManyRequests("Too Many Requests");
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#INTERNAL_SERVER_ERROR} status and the default message
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException internalServerError() {
        return internalServerError("Internal Server Error");
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#NOT_IMPLEMENTED} status and the default message
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException notImplemented() {
        return notImplemented("Not Implemented");
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#SERVICE_UNAVAILABLE} status and the default message
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException serviceUnavailable() {
        return serviceUnavailable("Service Unavailable");
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#GATEWAY_TIMEOUT} status and the default message
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException gatewayTimeout() {
        return gatewayTimeout("Gateway Timeout");
    }

    /**
     * Shorthand to create a new instance of {@link ApiErrorException} with {@link HttpStatus#BAD_GATEWAY} status and the default message
     * @return a new instance of {@link ApiErrorException}
     */
    public static ApiErrorException badGateway() {
        return badGateway("Bad Gateway");
    }

}