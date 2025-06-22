package io.github.luidmidev.springframework.web.problemdetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
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

        controllerAdviceBeansMap.forEach((beanName, bean) -> {
            var beanClass = bean.getClass();
            // Verificar que el metodo acepte exactamente dos parámetros:
            // 1. Un tipo de Exception
            // 2. WebRequest
            // Y que el tipo de retorno sea ResponseEntity<Object>
            Arrays.stream(beanClass.getDeclaredMethods())
                    .filter(method -> !Modifier.isStatic(method.getModifiers()) && method.canAccess(bean) && method.isAnnotationPresent(ExceptionHandler.class))
                    .forEach(method -> {
                        var exceptionTypes = method.getParameterTypes();
                        if (exceptionTypes.length == 2
                                && Exception.class.isAssignableFrom(exceptionTypes[0])
                                && WebRequest.class.isAssignableFrom(exceptionTypes[1])
                                && isResponseEntityOfTypeObject(method)) {

                            log.debug("Found exception handler method: {} in bean: {}", method.getName(), beanName);
                            var exceptionClass = (Class<? extends Exception>) exceptionTypes[0];
                            exceptionHandlerMethods.put(exceptionClass, method);
                            controllerAdviceBeans.put(beanClass, bean);
                        }
                    });
        });
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
            var controllerAdviceBean = controllerAdviceBeans.get(handlerMethod.getDeclaringClass());
            return (ResponseEntity<Object>) handlerMethod.invoke(controllerAdviceBean, exception, webRequest);
        } catch (Exception e) {
            throw new IllegalStateException("Error invoking exception handler method", e);
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
