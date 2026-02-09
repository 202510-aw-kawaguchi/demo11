package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AsyncErrorNotifier {

    private static final Logger log = LoggerFactory.getLogger(AsyncErrorNotifier.class);

    public void notifyAsyncError(String methodName, Throwable ex) {
        log.warn("Notify async error for method {}", methodName, ex);
    }
}
