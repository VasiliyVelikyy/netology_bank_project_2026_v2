package org.example.service.virtual_thread;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.BankAccount;
import org.example.dto.ClientBalanceAndPhoneInfo;
import org.example.integration.PhoneWebClient;
import org.example.service.bank_account.BankAccountService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.example.util.Constants.PATH_PHONE_WITH_DELAY;
import static org.example.util.TimeUtil.evaluateExecutionTime;

@Slf4j
@Service
public class VirtualThreadClientAggregationService {
    private final BankAccountService bankAccountService;
    private final PhoneWebClient webClient;
    private final MeterRegistry meterRegistry;

    private final Semaphore semaphore;

    private final Counter submittedTasks;
    private final AtomicInteger activeTasks;
    private final Timer aggregationTimer;

    public VirtualThreadClientAggregationService(BankAccountService bankAccountService,
                                                 PhoneWebClient webClient,
                                                 MeterRegistry meterRegistry) {
        this.bankAccountService = bankAccountService;
        this.webClient = webClient;
        this.meterRegistry = meterRegistry;
        this.semaphore = new Semaphore(100);
        this.submittedTasks = Counter.builder("client.aggregation.virtual.threads.created")
                .description("Total number of submitted client aggregation tasks")
                .register(this.meterRegistry);

        this.activeTasks = this.meterRegistry.gauge(
                "client.aggregation.active.tasks",
                new AtomicInteger(0)
        );

        this.aggregationTimer = Timer.builder("client.aggregation.duration")
                .description("Time spent aggregating client information")
                .register(this.meterRegistry);
    }

    public List<ClientBalanceAndPhoneInfo> getFullClientInfoByVirtualThreads() {
        long startTime = System.nanoTime();
        log.info("Агрегация через виртуальные потоки ");
        var accounts = bankAccountService.findAll();

        log.debug("найдено счетов {}", accounts.size());

        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ClientBalanceAndPhoneInfo>> futures =
                    accounts.stream()
                            .map(acc ->
                                    executorService.submit(
                                            () -> {
                                                submittedTasks.increment();
                                                return getClientBalanceAndPhoneInfo(acc);
                                            }
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
            activeTasks.incrementAndGet();
            try {
                return aggregationTimer.record(() -> getClientInfo(acc));
            } finally {
                activeTasks.decrementAndGet();
                semaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private ClientBalanceAndPhoneInfo getClientInfo(BankAccount acc) {
        String phone = webClient.getPhoneNumberSync(acc.getAccountNumber(), PATH_PHONE_WITH_DELAY);
        return new ClientBalanceAndPhoneInfo(acc.getAccountNumber(), acc.getBalance(), phone);
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
