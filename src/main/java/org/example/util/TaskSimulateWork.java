package org.example.util;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.ResultOperation;

import static org.example.domain.ResultOperation.STOP;
import static org.example.domain.ResultOperation.SUCCESS;
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
        logResult(count);
    }

    public static ResultOperation simulateCpuWorkWithCancelIfInterrupted(long mills) {
        loggingStartThread();

        long start = System.currentTimeMillis();
        long count = 0;

        while (System.currentTimeMillis() - start < mills) {
            count += Math.sqrt(count + 1) % 10000;

            if (Thread.currentThread().isInterrupted()) {
                log.warn("Задача была отменена. Остановка cpu работы");
                return STOP;
            }
        }
        logResult(count);
        return SUCCESS;
    }


    private static void logResult(long count) {
        log.info("[{}] Завершено: {} итераций", Thread.currentThread().getName(), count);
    }

}
