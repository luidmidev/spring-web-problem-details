package io.github.luidmidev.springframework.web.problemdetails;

import org.springframework.http.ProblemDetail;

@FunctionalInterface
public interface UncaughtProblemDetailsCallback {
    void call(Exception exception, ProblemDetail problemDetail);

    static UncaughtProblemDetailsCallback none() {
        return (exception, problemDetails) -> {
            // No operation
        };
    }
}
