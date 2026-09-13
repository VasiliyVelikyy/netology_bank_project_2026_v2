package org.example.util;

import lombok.extern.slf4j.Slf4j;

import static org.example.util.LoggingUtils.loggingThreadError;

@Slf4j
public class ErrorHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        loggingThreadError((Exception) e);
    }
}
