package org.example.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeUtil {
    public static String evaluateExecutionTime(long startTime) {
        long endTime = System.nanoTime();
        long durationNanos = endTime - startTime;
        double durationSec = durationNanos / 1_000_000_000.0;
        String message = String.format("Время выполнения: %.3f секунд (%d наносекунды)",
                durationSec,
                durationNanos);
        printExecutionTime(message);
        return message;
    }

    private static void printExecutionTime(String message) {
        log.info(message);
    }
}
