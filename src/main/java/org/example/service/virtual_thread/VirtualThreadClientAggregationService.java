package org.example.service.virtual_thread;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.dto.ClientBalanceAndPhoneInfo;
import org.example.integration.PhoneWebClient;
import org.example.service.bank_account.BankAccountService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

import static org.example.util.Constants.PATH_PHONE_WITH_DELAY;
import static org.example.util.TimeUtil.evaluateExecutionTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class VirtualThreadClientAggregationService {
    private final BankAccountService bankAccountService;
    private final PhoneWebClient webClient;

    private final Semaphore semaphore = new Semaphore(100);

    public List<ClientBalanceAndPhoneInfo> getFullClientInfoByVirtualThreads() {
        long startTime = System.nanoTime();
        log.info("Агрегация через виртуальные потоки ");
        var accounts = bankAccountService.findAll();

        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ClientBalanceAndPhoneInfo>> futures =
                    accounts.stream()
                            .map(acc ->
                                    executorService.submit(
                                            () -> getClientBalanceAndPhoneInfo(acc)
                                    )).toList();

            var result = futures.stream()
                    .map(VirtualThreadClientAggregationService::unwrapInfo)
                    .toList();

            evaluateExecutionTime(startTime);

            return result;

        }
    }

    private static ClientBalanceAndPhoneInfo unwrapInfo(Future<ClientBalanceAndPhoneInfo> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            log.error("Ошибка отмены потока {}", e.getMessage());
            return new ClientBalanceAndPhoneInfo("N/A", 0.0, "InterruptedException");
        } catch (ExecutionException e) {
            log.error("Ошибка выполнения потока {}", e.getMessage());
            return new ClientBalanceAndPhoneInfo("N/A", 0.0, "ExecutionException");
        }

    }

    private ClientBalanceAndPhoneInfo getClientBalanceAndPhoneInfo(BankAccount acc) {
        try {
            semaphore.acquire();
            try {
                String phone = webClient.getPhoneNumberSync(acc.getAccountNumber(), PATH_PHONE_WITH_DELAY);
                return new ClientBalanceAndPhoneInfo(acc.getAccountNumber(), acc.getBalance(), phone);
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public String benchMark100k() {
        int tasks = 100_000;
        long sleep = 1000;

        long startVirtual = System.currentTimeMillis();

        try (ExecutorService vtService = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < tasks; i++) {
                vtService.submit(() -> {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
        long timeVirtual = System.currentTimeMillis() - startVirtual;

        long startPlatform = System.currentTimeMillis();
        try (ExecutorService ptService = Executors.newFixedThreadPool(1000)) {
            for (int i = 0; i < tasks; i++) {
                ptService.submit(() -> {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
        long timePlatform = System.currentTimeMillis() - startPlatform;

        return String.format("Virtual thread 100k %d ms | Platform threads (1k pool) %d ms",
                timeVirtual, timePlatform);

    }

    public List<ClientBalanceAndPhoneInfo> getFullClientInfoStructured() throws Exception {
        long startTime = System.nanoTime();
        List<BankAccount> accounts = bankAccountService.findAll();
        log.info("Начинаю агрегацию через StructuredTaskScope ({} аккаунтов)...", accounts.size());

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            List<StructuredTaskScope.Subtask<ClientBalanceAndPhoneInfo>> subtasks = accounts.stream()
                    .map(acc -> scope.fork(() -> {

                        if (acc.getAccountNumber().equals("GEN_ACC_-500")) {
                            log.warn("Симуляция сбоя для аккаунта: {}", acc.getAccountNumber());
                            throw new RuntimeException("Сбой внешнего сервиса");
                        }

                        if (Thread.currentThread().isInterrupted()) {
                            log.info(" Поток для {} прерван из-за сбоя в другом потоке", acc.getAccountNumber());
                            throw new RuntimeException("Task interrupted by Scope");
                        }

                        String phone = webClient.getPhoneNumberSyncStrict(
                                acc.getAccountNumber(), PATH_PHONE_WITH_DELAY);

                        return new ClientBalanceAndPhoneInfo(acc.getAccountNumber(), acc.getBalance(), phone);
                    }))
                    .toList();

            scope.join();

            scope.throwIfFailed();

            evaluateExecutionTime(startTime);

            return subtasks.stream()
                    .map(StructuredTaskScope.Subtask::get)
                    .toList();

        }
    }


}
