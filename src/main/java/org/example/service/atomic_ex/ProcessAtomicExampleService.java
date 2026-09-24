package org.example.service.atomic_ex;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.BankAccountState;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

import static org.example.util.TimeUtil.evaluateExecutionTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessAtomicExampleService {
    private final AtomicBankAccountService atomicBankAccountService;

    public String processAtomic() throws InterruptedException {
        int steps = 100_000;
        AtomicInteger atomicCounter = new AtomicInteger(0);

        int[] unsafeCounter = {0};

        Runnable writer = () -> {
            for (int i = 0; i < steps; i++) {
                unsafeCounter[0]++;
                atomicCounter.incrementAndGet();
            }
        };
        Thread t1 = new Thread(writer, "Writer1");
        Thread t2 = new Thread(writer, "Writer2");

        long startTime = System.nanoTime();

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        int expectedTotal = steps * 2;

        evaluateExecutionTime(startTime);
        log.info("Ожидаемый баланс {}", expectedTotal);
        log.info("Фактический баланс (небезопасный счетчик) {}  <--- {}  безопасный счетчик ",
                unsafeCounter[0], atomicCounter.get());

        return "оk";
    }

    public String processAtomicReference() throws InterruptedException {
        String accountNum = "ACC001";
        int steps = 1000;
        int threadsCount = 10;
        double depositAmount = 10.0;

        BankAccountState initialState = atomicBankAccountService.getAccountRef(accountNum).get();
        if (initialState == null) {
            log.error("Счёт {} не найден", accountNum);
            return "error";
        }

        double startBalance = initialState.balance();
        log.info("Начальный баланс (из БД): {}", startBalance);

        Thread[] workers = new Thread[threadsCount];

        for (int i = 0; i < threadsCount; i++) {
            final int threadId = i + 1;
            workers[i] = new Thread(() -> {
                for (int j = 0; j < steps; j++) {
                    atomicBankAccountService.deposit(accountNum, depositAmount);
                }
            }, "Поток-" + threadId);

            workers[i].start();
        }

        for (Thread worker : workers) {
            worker.join();
        }
        BankAccountState finalState = atomicBankAccountService.getAccountRef(accountNum).get();
        double expectedBalance = startBalance + (steps * depositAmount * threadsCount);

        log.info("Счёт: {} Ожидаемый баланс: {}", finalState.accountNumber(), expectedBalance);
        log.info("Счёт: {} Фактический баланс: {}", finalState.accountNumber(), finalState.balance());

        if (finalState.balance() == expectedBalance) {
            log.info("УСПЕХ. Данные не потеряны, несмотря на конфликты");
        }

        return "ok";
    }
}
