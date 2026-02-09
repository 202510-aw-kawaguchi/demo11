package com.example.demo.config;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

public class SimpleAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SimpleAsyncExceptionHandler.class);
    private final com.example.demo.service.AsyncErrorNotifier asyncErrorNotifier;

    public SimpleAsyncExceptionHandler(com.example.demo.service.AsyncErrorNotifier asyncErrorNotifier) {
        this.asyncErrorNotifier = asyncErrorNotifier;
    }

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("Async error in {} with params {}", method.getName(), params, ex);
        asyncErrorNotifier.notifyAsyncError(method.getName(), ex);
    }
}
