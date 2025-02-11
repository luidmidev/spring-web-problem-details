package io.github.luidmidev.springframework.web.problemdetails.test.controllers;

import io.github.luidmidev.springframework.web.problemdetails.ProblemDetails;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
        throw ProblemDetails.badRequest("Excepión de prueba");
    }

    /**
     * Endpoint that throws an exception with a bad request status
     * @return response entity
     */
    @GetMapping("bad-request-runtime")
    public ResponseEntity<String> errorWithRuntimeException() {
        throw new RuntimeException(ProblemDetails.badRequest("Excepión de ejecución de prueba"));
    }

    /**
     * Endpoint that throws an exception with a bad request status and an extension
     * @return response entity
     */
    @GetMapping("bad-request-by-extension")
    public ResponseEntity<String> errorWithExceptionByExtension() {
        throw ProblemDetails
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


    @PostMapping("request-parts")
    public ResponseEntity<String> requestParts(
            @RequestPart("part1") String part1,
            @RequestPart("part2") List<String> part2,
            @RequestPart("part3") MultipartFile part3
    ) {
        return ResponseEntity.ok("Part1: " + part1 + ", Part2: " + part2 + ", Part3: " + part3.getOriginalFilename());

    }

}
