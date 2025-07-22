package io.github.luidmidev.springframework.web.problemdetails;

import org.springframework.http.ProblemDetail;

@FunctionalInterface
public interface UncaughtProblemDetailCallback {
    void call(Exception exception, ProblemDetail problemDetail);

    static UncaughtProblemDetailCallback none() {
        return (exception, problemDetails) -> {
            // No operation
        };
    }
}
