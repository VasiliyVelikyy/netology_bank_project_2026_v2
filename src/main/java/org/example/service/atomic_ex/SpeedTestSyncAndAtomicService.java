package org.example.service.atomic_ex;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

import static org.example.util.Constants.ITERATION_FOR_SPEED_TEST;
import static org.example.util.LoggingUtils.loggingThreadError;
import static org.example.util.TimeUtil.evaluateExecutionTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpeedTestSyncAndAtomicService {
    private int syncBlockCount = 0;

    private final AtomicLong atomicCount = new AtomicLong();
    private final Object lock = new Object();
    private final int ITERATION = ITERATION_FOR_SPEED_TEST;


    public String processSpeedTestSyncAndAtomic() {
        log.info("Количество инкрементов {}", ITERATION);
        log.info("Ожидаемый итоговый результат {}", ITERATION * 2L);


        syncBlockCount = 0;
        atomicCount.set(0);

        long startSyncTime = System.nanoTime();
        testSyncronized(ITERATION);

        log.info("Результат syncronized");
        log.info("Итоговое значение счетчика {}", syncBlockCount);
        var syncTimeEnd = evaluateExecutionTime(startSyncTime);

        syncBlockCount = 0;

        long startAtomicTime = System.nanoTime();

        testAtomic(ITERATION);


        log.info("Результат atomic");
        log.info("Итоговое значение счетчика {}", atomicCount.get());
        var atomicTimeEnd = evaluateExecutionTime(startAtomicTime);

        atomicCount.set(0);

        log.info("Итоговое сравнение");
        log.info("Sync {}", syncTimeEnd);
        log.info("ATOMIC {}", atomicTimeEnd);
        log.info("Вывод atomic быстрее");

        return "ok";

    }

    private void testAtomic(int iteration) {
        Runnable task = () -> {
            for (int i = 0; i < iteration; i++) {
                atomicIncrement();

            }
        };
        startTasks(task);
    }

    private static void startTasks(Runnable task) {
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            loggingThreadError(e);
        }
    }

    private void atomicIncrement() {
        atomicCount.incrementAndGet();
    }

    private void testSyncronized(int iteration) {
        Runnable task = () -> {
            for (int i = 0; i < iteration; i++) {
                syncIncrement();

            }
        };
        startTasks(task);
    }

    private void syncIncrement() {
        synchronized (lock) {
            syncBlockCount++;
        }
    }
}
