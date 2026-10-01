package org.example.service.executor_services;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.service.bank_account.BankAccountService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class BalanceMonitorScheduler {
    public static final double THRESHOLD = 100.0;
    private final ExecutorService scheduler = createScheduler();
    private final BankAccountService bankAccountService;


    private void checkForHighBalance() {
        try {
            log.info("Запуск проверка достаточного баланса ");

            List<BankAccount> highBalanceAccounts = bankAccountService.findByBalanceGreaterThan(THRESHOLD);

            for (var acc : highBalanceAccounts) {
                String message = String.format("Внимание на аккаунте %s много денег %s", acc.getAccountNumber(),
                        acc.getBalance());
                log.warn(message);
                //todo sendNotificationAsync
            }
            logResult(highBalanceAccounts);
        } catch (Exception e) {
            log.error("Ошибка {}", e.getMessage(), e);
        }
    }

    private void checkForLowBalance() {
        try {
            log.info("Запуск проверки не достаточного баланса ");

            List<BankAccount> lowBalanceAccounts = bankAccountService.findByBalanceLessThan(THRESHOLD);

            for (var acc : lowBalanceAccounts) {
                String message = String.format("Внимание на аккаунте %s мало денег %s", acc.getAccountNumber(),
                        acc.getBalance());
                log.warn(message);
                //todo sendNotificationAsync
            }
            logResult(lowBalanceAccounts);
        } catch (Exception e) {
            log.error("Ошибка {}", e.getMessage(), e);
        }
    }

    private static void logResult(List<BankAccount> lowBalanceAccounts) {
        log.info("Проверка завершена, найдено аккантов {} с достаточным балансом",
                lowBalanceAccounts.size());
    }

    private ExecutorService createScheduler() {
        var scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "balanceMonitor");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(
                this::checkForLowBalance,
                0,
                10,
                TimeUnit.SECONDS
        );

        scheduler.scheduleAtFixedRate(
                this::checkForHighBalance,
                0,
                30,
                TimeUnit.SECONDS
        );
        return scheduler;

    }

    @PreDestroy
    public void shutdown() {
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
