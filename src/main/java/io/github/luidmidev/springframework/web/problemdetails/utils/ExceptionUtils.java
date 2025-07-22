package io.github.luidmidev.springframework.web.problemdetails.utils;

import lombok.experimental.UtilityClass;

import java.io.PrintWriter;
import java.io.StringWriter;

@UtilityClass
public class ExceptionUtils {
    /**
     * Get the stack trace of an exception as a string.
     *
     * @param throwable the exception
     * @return the stack trace as a string
     */
    public static String getStackTrace(Exception throwable) {
        if (throwable == null) return "";
        var sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }
}
