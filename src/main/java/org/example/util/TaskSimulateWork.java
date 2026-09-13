package org.example.util;

import lombok.extern.slf4j.Slf4j;

import static org.example.util.LoggingUtils.loggingStartThread;

@Slf4j
public class TaskSimulateWork {
    public static void simulateCpuWork(long mills) {
        loggingStartThread();

        long start = System.currentTimeMillis();
        long count = 0;

        while (System.currentTimeMillis() - start < mills) {
            count += Math.sqrt(count + 1) % 10000;
        }
        log.info("   [{}] Завершено: {} итераций", Thread.currentThread().getName(), count);
    }
}
