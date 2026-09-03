package org.example;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Поток " + t.getName() + " завершен с ошибкой " + e.getMessage());
    }
}
