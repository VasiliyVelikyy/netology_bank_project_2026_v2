package org.example.service.executor_services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.service.bank_account.BankAccountService;
import org.example.task.LowBalanceCheckTask;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationLowBalanceCheckService {
    private final BankAccountService bankAccountService;
    private final ExecutorService executorService = createNotifyExecutorService();

    public void triggerLowBalanceCheck() {
        List<BankAccount> accounts = bankAccountService.findAll();
        log.info("Запуск асинхронной проверки низкого баланса для {}, счетов", accounts.size());

        for (BankAccount account : accounts) {
            executorService.submit(new LowBalanceCheckTask(account.getBalance(), account.getAccountNumber()));
        }

        log.info("Все задачи отправлены в пул потоков. Проверка выполняеться в фоне");

    }

    private ExecutorService createNotifyExecutorService() {
        return Executors.newFixedThreadPool(5, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "NotifyBalance-" + counter.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        });
    }

}
