package org.example.task;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ForkJoinPool;

@Slf4j
public class LoggingUtils {

    public static void loggingCommonPool() {
        int cores = Runtime.getRuntime().availableProcessors();

        log.info("Доступно логических ядер {}", cores);

        ForkJoinPool commonPool = ForkJoinPool.commonPool();

        int parrallelism = ForkJoinPool.getCommonPoolParallelism();
        log.info("Целевой уровенл паралелизма {}", parrallelism);

        int poolSize = commonPool.getPoolSize();
        log.info("Текущий размер пула {}", poolSize);

        int activeCount = commonPool.getActiveThreadCount();
        log.info("Активных  птоков {}", activeCount);

        int runCount = commonPool.getRunningThreadCount();
        log.info("Потоки в состоянии running {}", runCount);

    }
}
