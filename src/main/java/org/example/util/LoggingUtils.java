package org.example.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ForkJoinPool;

@Slf4j
public class LoggingUtils {

    public static void loggingStartThread() {
        log.info("[ {} ] Стартовал.", Thread.currentThread().getName());
    }

    public static void loggingThreadError(Exception e) {
        log.error("Ошибка, threadName = {} , errorMessage = {}", Thread.currentThread().getName(), e.getMessage());
    }

    public static void loggingMoneyTransfer(String from, String to, double amount) {
        log.info("{}: Перевод {} с {} на {} выполнен.",
                Thread.currentThread().getName(), amount, from, to);
    }

    public static void loggingCustomPoolStats(ForkJoinPool pool) {
        log.info("СТАТИСТИКА CUSTOM POOL");
        log.info("Целевой параллелизм: {}", pool.getParallelism());
        log.info("Текущий размер пула (создано потоков): {}", pool.getPoolSize());
        log.info("Активных потоков (выполняют задачу): {}", pool.getActiveThreadCount());
        log.info("Успешных факторов кражи, (Work-Stealing): {}", pool.getStealCount()); // общее количество успешных фактов кражи
    }

    public static void loggingCommonPool() {
        int cores = Runtime.getRuntime().availableProcessors();
        log.info("Доступно логических ядер: {}", cores);

        ForkJoinPool commonPool = ForkJoinPool.commonPool();

        //  Целевой уровень параллелизма (максимальное кол-во рабочих потоков)
        int parallelism = ForkJoinPool.getCommonPoolParallelism();
        log.info("Целевой уровень параллелизма: {}", parallelism);

        // Фактическое количество созданных потоков в пуле на данный момент
        int poolSize = commonPool.getPoolSize();
        log.info("Текущий размер пула (создано потоков): {}", poolSize);

        //  Количество потоков, которые сейчас активно выполняют задачи
        int activeCount = commonPool.getActiveThreadCount();
        log.info("Активных потоков: {}", activeCount);

        //  Количество потоков, которые не заблокированы и готовы к работе
        int runningCount = commonPool.getRunningThreadCount();
        log.info("Потоков в состоянии running: {}", runningCount);
    }
}
