package com.example.demo.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class AsyncExceptionHandler implements AsyncConfigurer {

    private final Executor taskExecutor;
    private final com.example.demo.service.AsyncErrorNotifier asyncErrorNotifier;

    public AsyncExceptionHandler(@Qualifier("taskExecutor") Executor taskExecutor,
            com.example.demo.service.AsyncErrorNotifier asyncErrorNotifier) {
        this.taskExecutor = taskExecutor;
        this.asyncErrorNotifier = asyncErrorNotifier;
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncExceptionHandler(asyncErrorNotifier);
    }
}
