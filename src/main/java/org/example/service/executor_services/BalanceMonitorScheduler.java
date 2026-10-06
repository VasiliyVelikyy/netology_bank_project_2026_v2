package org.example.service.executor_services;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.service.bank_account.BankAccountService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class BalanceMonitorScheduler {
    public static final double THRESHOLD = 100.0;
    private final ScheduledExecutorService scheduler = createScheduler();
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

    private ScheduledExecutorService createScheduler() {
        var scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "balanceMonitor");
            t.setDaemon(true);
            return t;
        });

//        scheduler.scheduleAtFixedRate(
//                this::checkForLowBalance,
//                0,
//                10,
//                TimeUnit.SECONDS
//        );
//
//        scheduler.scheduleAtFixedRate(
//                this::checkForHighBalance,
//                0,
//                30,
//                TimeUnit.SECONDS
//        );
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

    public void scheduleOneTimeClick() {
        log.info("Планирование единичного отложенного запуска через 5 секунд");

        ScheduledFuture<?> future = scheduler.schedule(
                this::findAllAndPrintSize,
                5,
                TimeUnit.SECONDS
        );

        handleSchedulerResult(future);

    }

    private void findAllAndPrintSize() {
        try {
            log.info("Выполняется однократная проверка баланса");

            //нормальная ситуация
            // List<BankAccount> accounts = bankAccountService.findAll();

            //runtime exception
            //  List<BankAccount> accounts = bankAccountService.findAllWithException();

            //interrupted exception
            //  List<BankAccount> accounts = bankAccountService.findAllWithSleep();

            //cpu work with check cancel task and return
            List<BankAccount> accounts = bankAccountService.findAllWithCpuWork();

            log.info("Однократная проверка завершена. Всего счетов {}", accounts.size());
        } catch (Exception e) {
            log.error("Ошибка внутри однократной задачи", e);
            throw new CompletionException(e);
        }
    }

    private void handleSchedulerResult(ScheduledFuture<?> future) {
        try {
            future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException et) {
            log.error("Однократная задача не успела выполниться по таймауту");
            future.cancel(true);
        } catch (ExecutionException ex) {
            log.error("Задача завершилась с ошибкой (ExecutionException). Причина {}",
                    ex.getCause().getMessage(), ex.getCause());
        } catch (InterruptedException ie) {
            log.error("Поток был прерван во время ожидания задачи");
            Thread.currentThread().interrupt();
        }
    }

    public void demonstrateScheduledCallable() {
        log.info("Однократная задача с возвратом результат");

        ScheduledFuture<Double> future = scheduler.schedule(this::getAllSumByAccount,
                1,
                TimeUnit.SECONDS);

        try {
            log.info("основной поток не блокируется");
            double result = future.get(5, TimeUnit.SECONDS);

            log.info("Данные получены. Общая сумма= {}", result);
        } catch (TimeoutException e) {
            log.warn("Ошибка таймаута. Можно использовать будущий кеш");
            //todo в месте проверить is interrupted и остановку задачи
            future.cancel(true);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private double getAllSumByAccount() throws InterruptedException {
        log.info("Запрос к бд по всем счетам");
        Thread.sleep(7000);
        return 92.50;
    }
}
