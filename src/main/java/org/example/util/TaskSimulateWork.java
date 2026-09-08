package org.example.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskSimulateWork {
    public static void simulateCpuWork(String name, long mills) {
        log.info("[ " + Thread.currentThread().getName() + " ] " + " активная работа");

        long start = System.currentTimeMillis();
        long count = 0;

        while (System.currentTimeMillis() - start < mills) {
            count += Math.sqrt(count + 1) % 10000;
        }
        log.info("[ " + Thread.currentThread().getName() + " ] Завершено, количество итераций " + count);
    }
}
