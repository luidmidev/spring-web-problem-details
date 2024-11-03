package io.github.luidmidev.springframework.web.problemdetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@RequiredArgsConstructor
public class ResponseEntityExceptionHandlerResolver {

    private final ApplicationContext applicationContext;
    private final Map<Class<?>, Object> controllerAdviceBeans = new HashMap<>();
    private final Map<Class<? extends Exception>, Method> exceptionHandlerMethods = new HashMap<>();
    private final Map<Class<? extends Exception>, Method> handlerMethodCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @EventListener(ApplicationStartedEvent.class)
    private void initializeExceptionHandlers() {
        var controllerAdviceBeansMap = applicationContext.getBeansWithAnnotation(ControllerAdvice.class);
        var restControllerAdviceBeansMap = applicationContext.getBeansWithAnnotation(RestControllerAdvice.class);

        controllerAdviceBeansMap.putAll(restControllerAdviceBeansMap);
        controllerAdviceBeansMap.forEach((key, value) -> log.debug("Found controller advice bean: {}", key));

        for (var bean : controllerAdviceBeansMap.entrySet()) {
            var beanClass = bean.getValue().getClass();

            for (var method : beanClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(ExceptionHandler.class)) {
                    var exceptionTypes = method.getParameterTypes();

                    // Verificar que el metodo acepte exactamente dos parámetros:
                    // 1. Un tipo de Exception
                    // 2. WebRequest
                    // Y que el tipo de retorno sea ResponseEntity<Object>
                    if (exceptionTypes.length == 2
                            && Exception.class.isAssignableFrom(exceptionTypes[0])
                            && WebRequest.class.isAssignableFrom(exceptionTypes[1])
                            && isResponseEntityOfTypeObject(method)) {

                        log.debug("Found exception handler method: {} in bean: {}", method.getName(), bean.getKey());
                        var exceptionClass = (Class<? extends Exception>) exceptionTypes[0];
                        exceptionHandlerMethods.put(exceptionClass, method);
                        controllerAdviceBeans.put(beanClass, bean.getValue());
                    }
                }
            }
        }
    }

    private static boolean isResponseEntityOfTypeObject(Method method) {
        if (method.getReturnType().equals(ResponseEntity.class)) {
            var genericReturnType = (ParameterizedType) method.getGenericReturnType();
            return genericReturnType.getActualTypeArguments()[0].equals(Object.class);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> handleException(Exception exception, WebRequest webRequest) throws NoSuchMethodException {
        var exceptionClass = exception.getClass();
        var handlerMethod = findHandlerMethod(exceptionClass);

        if (handlerMethod == null) {
            throw new NoSuchMethodException("No handler found for exception: " + exception.getClass().getName());
        }

        try {
            handlerMethod.setAccessible(true);
            var controllerAdviceBean = controllerAdviceBeans.get(handlerMethod.getDeclaringClass());
            return (ResponseEntity<Object>) handlerMethod.invoke(controllerAdviceBean, exception, webRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error invoking exception handler method", e);
        }

    }


    private Method findHandlerMethod(Class<? extends Exception> exceptionClass) {

        if (handlerMethodCache.containsKey(exceptionClass)) {
            return handlerMethodCache.get(exceptionClass);
        }

        Method closestHandlerMethod = null;
        Class<?> currentClass = exceptionClass;

        while (currentClass != null && closestHandlerMethod == null) {
            closestHandlerMethod = exceptionHandlerMethods.get(currentClass);
            currentClass = currentClass.getSuperclass();
        }

        handlerMethodCache.put(exceptionClass, closestHandlerMethod);
        return closestHandlerMethod;
    }

}
