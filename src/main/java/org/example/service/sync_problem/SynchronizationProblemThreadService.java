package org.example.service.sync_problem;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.service.bank_account.BankAccountProfilingExampleService;
import org.example.service.bank_account.BankAccountService;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.example.util.LoggingUtils.loggingThreadError;

@Slf4j
@Service
@RequiredArgsConstructor
public class SynchronizationProblemThreadService {
    public static final String ACC_1 = "ACC001";
    public static final String ACC_2 = "ACC002";

    private final BankAccountService bankAccountService;
    private final BankAccountProfilingExampleService profilingExampleService;

    @Transactional
    public String processRaceCondition() {

        Thread fasterTheadOne = new Thread(() -> {
            try {
                bankAccountService.transferForStreamBlockOneMonitor(ACC_1, ACC_2, 50);
            } catch (Exception e) {
                loggingThreadError(e);
            }

        }, "Faster thread 1");

        Thread fasterTheadTwo = new Thread(() -> {
            try {
                bankAccountService.transferForStreamBlockOneMonitor(ACC_1, ACC_2, 50);
            } catch (Exception e) {
                loggingThreadError(e);
            }
        }, "Faster thread 2");

        fasterTheadOne.start();
        fasterTheadTwo.start();

        try {
            fasterTheadOne.join();
            fasterTheadTwo.join();
        } catch (InterruptedException e) {
            loggingThreadError(e);
        }

        return getFinalBalanceMessage(bankAccountService.getAccount(ACC_1));
    }

    public String processDeadLock() {

        Thread t1 = new Thread(() -> {
            try {
                bankAccountService.transferWithDoubleSync(ACC_1, ACC_2, 50);
            } catch (Exception e) {
                loggingThreadError(e);
            }

        }, "Deadlock thread 1");

        Thread t2 = new Thread(() -> {
            try {
                bankAccountService.transferWithDoubleSync(ACC_2, ACC_1, 50);
            } catch (Exception e) {
                loggingThreadError(e);
            }
        }, "Deadlock thread 2");

        t1.start();
        t2.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            loggingThreadError(e);
        }

        String message;
        if (t1.isAlive() && t2.isAlive()) {
            message = "Deadlock обнаружен - оба потока зависли";
        } else {
            message = "Deadlock не обнаружен";
        }
        log.info(message);
        return message;
    }

    public String processLiveLock() {
        AtomicBoolean client1Moving = new AtomicBoolean(false);
        AtomicBoolean client2Moving = new AtomicBoolean(false);
        AtomicBoolean stopDemo = new AtomicBoolean(false);

        Thread client1 = new Thread(() -> {

            while (!stopDemo.get()) {
                client1Moving.set(true);

                log.info("[LIVELOCK] Client-1: пытаюсь пройти");

                while (!client2Moving.get() && !stopDemo.get()) {
                    Thread.onSpinWait();
                }

                if (stopDemo.get()) {
                    break;
                }

                log.info("[LIVELOCK] Client-1: вижу Client-2 → уступаю");

                client1Moving.set(false);
            }

            log.info("[LIVELOCK] Client-1 завершён");

        }, "Client-1");


        Thread client2 = new Thread(() -> {

            while (!stopDemo.get()) {

                client2Moving.set(true);

                log.info("[LIVELOCK] Client-2: пытаюсь пройти");

                while (!client1Moving.get() && !stopDemo.get()) {
                    Thread.onSpinWait();
                }

                if (stopDemo.get()) {
                    break;
                }

                log.info("[LIVELOCK] Client-2: вижу Client-1 → уступаю");

                client2Moving.set(false);
            }

            log.info("[LIVELOCK] Client-2 завершён");

        }, "Client-2");


        client1.start();
        client2.start();


        try {
            Thread.sleep(20_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        stopDemo.set(true);


        return "Livelock продемонстрирован — потоки активно работают, но прогресса нет.";
    }

    public String transferStarvation() throws InterruptedException {
        Lock lock = new ReentrantLock();

        AtomicBoolean stopDemo = new AtomicBoolean(false);
        AtomicLong greedyCount = new AtomicLong(0);
        AtomicLong starvingCount = new AtomicLong(0);

        Thread greedyThread = new Thread(() -> {
            while (!stopDemo.get()) {
                try {
                    profilingExampleService.transferWithPark(ACC_1, ACC_2, lock, 1);
                    greedyCount.incrementAndGet();
                } catch (Exception e) {
                    loggingThreadError(e);
                }
            }
            log.info("Жадный поток завершён. Успешных переводов: {}", greedyCount.get());
        }, "Greedy Thread");

        Thread starvingThread = new Thread(() -> {
            while (!stopDemo.get()) {
                try {
                    profilingExampleService.transferWithPark(ACC_2, ACC_1, lock, 10);
                    starvingCount.incrementAndGet();

                    //Thread.sleep(1000);
                } catch (Exception e) {
                    loggingThreadError(e);
                }
            }
            log.info("Голодающий поток завершён. Успешных переводов: {}", starvingCount.get());
        }, "Starving Thread");

        greedyThread.start();
        starvingThread.start();

        log.info("Демонстрация голодания запущена на 10 секунд...");
        Thread.sleep(10_000);

        log.info("Останавливаем оба потока...");
        stopDemo.set(true);

        greedyThread.join(2000);
        starvingThread.join(2000);

        return String.format("Starvation продемонстрирован! Жадный поток сделал %d переводов, а голодающий всего %d.",
                greedyCount.get(), starvingCount.get());
    }

    public String transferLivelockWithMaxAttempt() throws InterruptedException {
        final int MAX_ATTEMPTS = 10;

        AtomicBoolean client1Moving = new AtomicBoolean(false);
        AtomicBoolean client2Moving = new AtomicBoolean(false);
        AtomicBoolean stopDemo = new AtomicBoolean(false);

        Thread client1 = new Thread(() -> {
            int attempts = 0;

            try {
                while (!stopDemo.get() && attempts < MAX_ATTEMPTS) {

                    attempts++;

                    client1Moving.set(true);

                    log.info("[LIVELOCK] Client-1: пытаюсь пройти, попытка {}", attempts);

                    while (!client2Moving.get() && !stopDemo.get()) {
                        Thread.onSpinWait();
                    }

                    if (stopDemo.get()) {
                        break;
                    }

                    log.info("[LIVELOCK] Client-1: вижу Client-2 → уступаю");

                    client1Moving.set(false);

                    Thread.yield();
                }

                if (attempts >= MAX_ATTEMPTS) {
                    stopDemo.set(true);
                }

            } finally {
                client1Moving.set(false);
                log.info("[LIVELOCK] Client-1 завершён после {} попыток", attempts);
            }

        }, "Client-1");


        Thread client2 = new Thread(() -> {
            int attempts = 0;
            try {
                while (!stopDemo.get() && attempts < MAX_ATTEMPTS) {
                    attempts++;
                    client2Moving.set(true);

                    log.info("[LIVELOCK] Client-2: пытаюсь пройти, попытка {}", attempts);

                    while (!client1Moving.get() && !stopDemo.get()) {
                        Thread.onSpinWait();
                    }

                    if (stopDemo.get()) {
                        break;
                    }

                    log.info("[LIVELOCK] Client-2: вижу Client-1 → уступаю");

                    client2Moving.set(false);

                    Thread.yield();
                }

                if (attempts >= MAX_ATTEMPTS) {
                    stopDemo.set(true);
                }

            } finally {
                client2Moving.set(false);
                log.info("[LIVELOCK] Client-2 завершён после {} попыток", attempts);
            }

        }, "Client-2");


        client1.start();
        client2.start();

        client1.join();
        client2.join();

        return "Livelock продемонстрирован — потоки сделали попытки без прогресса.";
    }

    private String getFinalBalanceMessage(BankAccount account) {
        String message = String.format("Итоговый баланс  accNum =%s ,balance =%s",
                account.getAccountNumber(),
                account.getBalance());
        log.info(message);
        return message;
    }
}
