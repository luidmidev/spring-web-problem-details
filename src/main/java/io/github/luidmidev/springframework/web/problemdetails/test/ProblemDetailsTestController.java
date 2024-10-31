package io.github.luidmidev.springframework.web.problemdetails.test;

import io.github.luidmidev.springframework.web.problemdetails.ApiError;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller
 */
@RestController
@Profile("test")
public class ProblemDetailsTestController {

    /**
     * Endpoint that throws an exception with a bad request status
     * @return response entity
     */
    @GetMapping("bad-request")
    public ResponseEntity<String> errorWithException() {
        throw ApiError.badRequest("Excepión de prueba");
    }

    /**
     * Endpoint that throws an exception with a bad request status and an extension
     * @return response entity
     */
    @GetMapping("bad-request-by-extension")
    public ResponseEntity<String> errorWithExceptionByExtension() {
        throw ApiError
                .status(HttpStatus.BAD_REQUEST)
                .title("This is a test exception")
                .type("https://example.com/errors/test-exception")
                .instance("https://example.com/errors/test-exception/1")
                .extension("field1", "value1")
                .extension("field2", "value2")
                .detail("This is a test exception");

    }

    /**
     * Endpoint that throws an exception with a not found status
     * @param param the parameter
     * @return response entity
     */
    @GetMapping("endpoint-with-param")
    public ResponseEntity<String> endpointWithParam(@RequestParam String param) {
        return ResponseEntity.ok("Param: " + param);
    }
}
